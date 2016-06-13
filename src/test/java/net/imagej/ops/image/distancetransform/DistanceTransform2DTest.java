package net.imagej.ops.image.distancetransform;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class DistanceTransform2DTest extends AbstractOpTest {

	final static double EPSILON = 0.1;

	@Test
	public void test() {
		for (int i = 0; i < 10; i++) {
			// create 2D image
			Img<BitType> in = ops.convert().bit(ops.create().img(new int[] { 40, 40 }));
			generate2DImg(in);

			Random random = new Random();
			double[] calibration = new double[] { random.nextDouble() * 5, random.nextDouble() * 5 };

			// output of DT ops
			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<FloatType> out = (RandomAccessibleInterval<FloatType>) ops
					.run(DistanceTransform2D.class, null, in, calibration);

			// assertEquals
			compareResults(out, in, calibration);
		}
	}

	/*
	 * generate a random BitType image
	 */
	private void generate2DImg(RandomAccessibleInterval<BitType> in) {
		RandomAccess<BitType> raIn = in.randomAccess();

		Random random = new Random();
		for (int x = 0; x < in.dimension(0); x++) {
			for (int y = 0; y < in.dimension(1); y++) {
				raIn.setPosition(new int[] { x, y });
				raIn.get().set(random.nextBoolean());
			}
		}
	}

	/*
	 * "trivial" distance transform algorithm -> calculate distance to each
	 * pixel and select the shortest
	 */
	private void compareResults(RandomAccessibleInterval<FloatType> out, RandomAccessibleInterval<BitType> in,
			double[] calibration) {
		RandomAccess<FloatType> raOut = out.randomAccess();
		RandomAccess<BitType> raIn = in.randomAccess();
		int fail = 0;
		for (int x0 = 0; x0 < in.dimension(0); x0++) {
			for (int y0 = 0; y0 < in.dimension(1); y0++) {
				raIn.setPosition(new int[] { x0, y0 });
				raOut.setPosition(new int[] { x0, y0 });
				if (!raIn.get().get()) {
					assertEquals(0, raOut.get().get(), EPSILON);
					if (Double.compare(0, raOut.get().get()) != 0)
						fail++;
				} else {
					double actualValue = in.dimension(0) * in.dimension(0) + in.dimension(1) * in.dimension(1);
					for (int x = 0; x < in.dimension(0); x++) {
						for (int y = 0; y < in.dimension(1); y++) {
							raIn.setPosition(new int[] { x, y });
							double dist = calibration[0] * calibration[0] * (x0 - x) * (x0 - x)
									+ calibration[1] * calibration[1] * (y0 - y) * (y0 - y);
							if ((!raIn.get().get()) && (dist < actualValue))
								actualValue = dist;
						}
					}
					assertEquals(Math.sqrt(actualValue), raOut.get().get(), EPSILON);
					if (Math.abs((Math.sqrt(actualValue) - raOut.get().get())) > EPSILON)
						fail++;
				}
			}
		}
		System.out.println(fail);
	}
}
