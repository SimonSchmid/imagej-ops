package net.imagej.ops.image.distancetransform;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import net.imagej.ops.AbstractOpTest;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;

public class DefaultDistanceTransformTest extends AbstractOpTest {

	final static double EPSILON = 0.05;

	@Test
	public void test() {
		// create 4D image
		Img<BitType> in = ops.convert().bit(ops.create().img(new int[] { 20, 20, 5, 3 }));
		generate4DImg(in);

		Random random = new Random();
		double[] calibration = new double[] { random.nextDouble() * 5, random.nextDouble() * 5, random.nextDouble() * 5,
				random.nextDouble() * 5 };

		// output of DT ops
		@SuppressWarnings("unchecked")
		RandomAccessibleInterval<FloatType> out = (RandomAccessibleInterval<FloatType>) ops
				.run(DefaultDistanceTransform.class, null, in, calibration);

		// assertEquals
		compareResults(out, in, calibration);
	}

	/*
	 * generate a random BitType image
	 */
	private void generate4DImg(RandomAccessibleInterval<BitType> in) {
		RandomAccess<BitType> raIn = in.randomAccess();
		Random random = new Random();
		for (int x = 0; x < in.dimension(0); x++) {
			for (int y = 0; y < in.dimension(1); y++) {
				for (int z = 0; z < in.dimension(2); z++) {
					for (int w = 0; w < in.dimension(3); w++) {
						raIn.setPosition(new int[] { x, y, z, w });
						raIn.get().set(random.nextBoolean());
					}
				}
			}
		}
	}

	/*
	 * "trivial" distance transform algorithm -> calculate distance to each
	 * pixel and select the shortest
	 */
	private void compareResults(final RandomAccessibleInterval<FloatType> out,
			final RandomAccessibleInterval<BitType> in, final double[] calibration) {
		RandomAccess<FloatType> raOut = out.randomAccess();
		RandomAccess<BitType> raIn = in.randomAccess();

		for (int x0 = 0; x0 < in.dimension(0); x0++) {
			for (int y0 = 0; y0 < in.dimension(1); y0++) {
				for (int z0 = 0; z0 < in.dimension(2); z0++) {
					for (int w0 = 0; w0 < in.dimension(3); w0++) {
						raIn.setPosition(new int[] { x0, y0, z0, w0 });
						raOut.setPosition(new int[] { x0, y0, z0, w0 });
						if (!raIn.get().get())
							assertEquals(0, raOut.get().get(), EPSILON);
						else {
							double actualValue = in.dimension(0) * in.dimension(0) + in.dimension(1) * in.dimension(1)
									+ in.dimension(2) * in.dimension(2) + in.dimension(3) * in.dimension(3);
							for (int x = 0; x < in.dimension(0); x++) {
								for (int y = 0; y < in.dimension(1); y++) {
									for (int z = 0; z < in.dimension(2); z++) {
										for (int w = 0; w < in.dimension(3); w++) {
											raIn.setPosition(new int[] { x, y, z, w });
											double dist = calibration[0] * calibration[0] * (x0 - x) * (x0 - x)
													+ calibration[1] * calibration[1] * (y0 - y) * (y0 - y)
													+ calibration[2] * calibration[2] * (z0 - z) * (z0 - z)
													+ calibration[3] * calibration[3] * (w0 - w) * (w0 - w);

											if ((!raIn.get().get()) && (dist < actualValue))
												actualValue = dist;
										}
									}
								}
							}
							assertEquals(Math.sqrt(actualValue), raOut.get().get(), EPSILON);
						}
					}
				}
			}
		}
	}
}
