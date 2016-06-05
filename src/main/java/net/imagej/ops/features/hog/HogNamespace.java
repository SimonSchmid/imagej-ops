package net.imagej.ops.features.hog;

import org.scijava.plugin.Plugin;

import net.imagej.ops.AbstractNamespace;
import net.imagej.ops.Namespace;
import net.imagej.ops.OpMethod;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

/**
 * Namespace for Histogram of oriented gradients Features
 * 
 * @author Simon Schmid, University of Konstanz
 */
@Plugin(type = Namespace.class)
public class HogNamespace extends AbstractNamespace {

	@Override
	public String getName() {
		return "hog";
	}

	// -- histogram of oriented gradients --

	/** Executes the "hog" operation on the given arguments. */	
	@OpMethod(op = net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> hog(final RandomAccessibleInterval<T> in) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result =
			(RandomAccessibleInterval<T>) ops().run(net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class, in);
		return result;
	}

	/** Executes the "hog" operation on the given arguments. */
	@OpMethod(op = net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> hog(final RandomAccessibleInterval<T> out, final RandomAccessibleInterval<T> in) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result =
			(RandomAccessibleInterval<T>) ops().run(net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class, out, in);
		return result;
	}

	/** Executes the "hog" operation on the given arguments. */
	@OpMethod(op = net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> hog(final RandomAccessibleInterval<T> out, final RandomAccessibleInterval<T> in, final int numBins) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result =
			(RandomAccessibleInterval<T>) ops().run(net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class, out, in, numBins);
		return result;
	}

	/** Executes the "hog" operation on the given arguments. */
	@OpMethod(op = net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class)
	public <T extends RealType<T>> RandomAccessibleInterval<T> hog(final RandomAccessibleInterval<T> out, final RandomAccessibleInterval<T> in, final int numBins, final int spanOfNeighborhood) {
		@SuppressWarnings("unchecked")
		final RandomAccessibleInterval<T> result =
			(RandomAccessibleInterval<T>) ops().run(net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class, out, in, numBins, spanOfNeighborhood);
		return result;
	}

}
