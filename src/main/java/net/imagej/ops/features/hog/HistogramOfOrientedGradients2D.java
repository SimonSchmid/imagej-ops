package net.imagej.ops.features.hog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;

import net.imagej.ops.Contingent;
import net.imagej.ops.Ops;
import net.imagej.ops.create.img.CreateImgFromDimsAndType;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imagej.ops.special.hybrid.AbstractUnaryHybridCF;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsAccessible;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.CompositeView;
import net.imglib2.view.composite.RealComposite;

/**
 * @author Simon Schmid (University of Konstanz)
 */
@Plugin(type = Ops.HoG.HistogramOfOrientedGradients.class)
public class HistogramOfOrientedGradients2D<T extends RealType<T>>
		extends AbstractUnaryHybridCF<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>
		implements Ops.HoG.HistogramOfOrientedGradients, Contingent {

	@Parameter(required = false)
	private int numBins;

	@Parameter(required = false)
	private int spanOfNeighborhood;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<FinalInterval, RandomAccessibleInterval> createOp;

	private ExecutorService es;

	@Parameter
	private ThreadService ts;

	@Override
	public void initialize() {
		es = ts.getExecutorService();

		if (numBins == 0)
			numBins = 9;
		if (spanOfNeighborhood == 0)
			spanOfNeighborhood = 2;

		createOp = Functions.unary(ops(), CreateImgFromDimsAndType.class, RandomAccessibleInterval.class, new FinalInterval(in().dimension(0), in().dimension(1), numBins),
				new FloatType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public RandomAccessibleInterval<T> createOutput(RandomAccessibleInterval<T> in) {
		return createOp.compute1(new FinalInterval(in().dimension(0), in().dimension(1), numBins));
	}

	@Override
	public boolean conforms() {
		return in().numDimensions() == 2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void compute1(RandomAccessibleInterval<T> in, RandomAccessibleInterval<T> out) {
		// compute partial derivative for each dimension
		RandomAccessibleInterval<T> derivative0 = (RandomAccessibleInterval<T>) ops().create().img(in);
		RandomAccessibleInterval<T> derivative1 = (RandomAccessibleInterval<T>) ops().create().img(in);
		PartialDerivative.gradientCentralDifference(Views.extendMirrorSingle(in), derivative0, 0);
		PartialDerivative.gradientCentralDifference(Views.extendMirrorSingle(in), derivative1, 1);

		// store them in a CompositeView
		CompositeIntervalView<T, RealComposite<T>> derivatives = Views
				.collapseReal(Views.stack(derivative0, derivative1));

		// compute angles and magnitudes
		RandomAccessibleInterval<T> angles = (RandomAccessibleInterval<T>) ops().create().img(in);
		RandomAccessibleInterval<T> magnitudes = (RandomAccessibleInterval<T>) ops().create().img(in);
		CompositeView<T, RealComposite<T>>.CompositeRandomAccess raDerivatives = derivatives.randomAccess();
		RandomAccess<T> raMagnitudes = magnitudes.randomAccess();
		RandomAccess<T> raAngles = angles.randomAccess();
		for (int i = 0; i < in.dimension(0); i++) {
			for (int j = 0; j < in.dimension(1); j++) {
				final long[] pos = new long[] { i, j };
				raDerivatives.setPosition(pos);
				raAngles.setPosition(pos);
				raMagnitudes.setPosition(pos);
				raAngles.get().setReal(
						getAngle(raDerivatives.get().get(0).getRealFloat(), raDerivatives.get().get(1).getRealFloat()));
				raMagnitudes.get().setReal(getMagnitude(raDerivatives.get().get(0).getRealFloat(),
						raDerivatives.get().get(1).getRealFloat()));
			}
		}

		// stores each Thread to execute
		final List<Callable<Void>> listCallables = new ArrayList<>();

		// compute descriptor (default 3x3, i.e. 9 channels: one channel for
		// each bin)
		final RectangleShape shape = new RectangleShape(spanOfNeighborhood, false);

		for (int i = 0; i < in.dimension(0); i++) {
			listCallables.add(new ComputeDescriptor(in, i, raAngles.copyRandomAccess(), raMagnitudes.copyRandomAccess(),
					out.randomAccess(), shape));
		}
		try {
			es.invokeAll(listCallables);
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}

		listCallables.clear();

	}

	class ComputeDescriptor implements Callable<Void> {
		final private RandomAccessibleInterval<T> in;
		final private long i;
		final private RandomAccess<T> raAngles;
		final private RandomAccess<T> raMagnitudes;
		final private RandomAccess<T> raOut;
		final private RectangleShape shape;

		public ComputeDescriptor(final RandomAccessibleInterval<T> in, final long i, final RandomAccess<T> raAngles,
				final RandomAccess<T> raMagnitudes, final RandomAccess<T> raOut, final RectangleShape shape) {
			this.in = in;
			this.i = i;
			this.raAngles = raAngles;
			this.raMagnitudes = raMagnitudes;
			this.raOut = raOut;
			this.shape = shape;
		}

		@Override
		public Void call() throws Exception {
			for (int j = 0; j < in.dimension(1); j++) {
				// sum up the magnitudes of all bins in a neighborhood
				NeighborhoodsAccessible<T> neighborHood = shape.neighborhoodsRandomAccessible(in);
				RandomAccess<Neighborhood<T>> raNeighbor = neighborHood.randomAccess();
				raNeighbor.setPosition(new long[] { i, j });
				Cursor<T> cursorNeighborHood = raNeighbor.get().cursor();

				final long[] posNeighbor = new long[cursorNeighborHood.numDimensions()];
				while (cursorNeighborHood.hasNext()) {
					cursorNeighborHood.next();
					cursorNeighborHood.localize(posNeighbor);
					if (Intervals.contains(in, new Point(posNeighbor))) {
						raAngles.setPosition(posNeighbor);
						raMagnitudes.setPosition(posNeighbor);
						long[] newPos = new long[3];
						newPos[0] = posNeighbor[0];
						newPos[1] = posNeighbor[1];
						newPos[2] = (int) raAngles.get().getRealDouble() / (360 / numBins);
						raOut.setPosition(newPos);
						raOut.get().setReal(raOut.get().getRealDouble() + raMagnitudes.get().getRealDouble());
					}
				}
			}
			return null;
		}

	}

	private double getAngle(final double x, final double y) {
		float angle = (float) Math.toDegrees(Math.atan2(x, y));

		if (angle < 0) {
			angle += 360;
		}

		return angle;
	}

	private double getMagnitude(final double x, final double y) {

		return Math.sqrt(x * x + y * y);
	}

}
