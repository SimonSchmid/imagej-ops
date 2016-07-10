package net.imagej.ops.topology.eulerCharacteristic;

import net.imagej.ops.Contingent;
import net.imagej.ops.Ops;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.BooleanType;
import net.imglib2.view.Views;
import org.scijava.plugin.Plugin;

import java.util.stream.LongStream;

/**
 * An Op which calculates the correction needed to approximate the contribution of the image to the
 * Euler characteristic χ of the whole image. That is, it's assumed that the image is a small part cut
 * from a larger sample.
 * <p>
 * From Odgaard & Gundersen (see below): "-- the Euler characteristic of the entire 3-D space will not be obtained
 * by simply adding the Euler characteristics of cubic specimens.
 * By doing this, the contribution of the lower dimensional elements will not be considered".
 * They give the correction as c = -1/2χ_2 - 1/4χ_1 - -1/8χ_0, where χ_2 = χ of all the faces,
 * χ_1 = χ of all the edges, and χ_0 = χ of all the corner vertices of the stack.
 * Each face contributes to two, each edge to four, and each corner to eight stacks.
 * <p>
 * Odgaard A, Gundersen HJG (1993) Quantification of connectivity in cancellous bone,
 * with special emphasis on 3-D reconstructions.
 * Bone 14: 173-182.
 * <a href="http://dx.doi.org/10.1016/8756-3282(93)90245-6">doi:10.1016/8756-3282(93)90245-6</a>
 *
 * @author Michael Doube (Royal Veterinary College, London)
 * @author Richard Domander (Royal Veterinary College, London)
 * @implNote Methods are public and static to help testing
 */
@Plugin(type = Ops.Topology.EulerCorrection.class)
public class EulerCorrection<B extends BooleanType<B>> extends AbstractUnaryFunctionOp<RandomAccessibleInterval<B>, Double>
        implements Ops.Topology.EulerCorrection, Contingent {
    /** The algorithm is defined only for 3D images */
    @Override
    public boolean conforms() {
        return in().numDimensions() == 3;
    }

    @Override
    public Double compute1(RandomAccessibleInterval<B> interval) {
        final Traverser<B> traverser = new Traverser<>(interval);
        final long chiZero = stackCorners(traverser);
        final long e = stackEdges(traverser) + 3 * chiZero;
        final long d = voxelEdgeIntersections(traverser) + chiZero;
        final long c = stackFaces(traverser) + 2 * e - 3 * chiZero;
        final long b = voxelEdgeFaceIntersections(traverser);
        final long a = voxelFaceIntersections(traverser);

        final long chiOne = d - e;
        final long chiTwo = a - b + c;

        return chiTwo / 2.0 + chiOne / 4.0 + chiZero / 8.0;
    }

    /**
     * Counts the foreground voxels in stack corners
     * <p>
     * Calculates χ_0 from Odgaard and Gundersen
     */
    public static <B extends BooleanType<B>> int stackCorners(final Traverser<B> traverser) {
        int foregroundVoxels = 0;
        foregroundVoxels += getAtLocation(traverser, traverser.x0, traverser.y0, traverser.z0);
        foregroundVoxels += getAtLocation(traverser, traverser.x1, traverser.y0, traverser.z0);
        foregroundVoxels += getAtLocation(traverser, traverser.x1, traverser.y1, traverser.z0);
        foregroundVoxels += getAtLocation(traverser, traverser.x0, traverser.y1, traverser.z0);
        foregroundVoxels += getAtLocation(traverser, traverser.x0, traverser.y0, traverser.z1);
        foregroundVoxels += getAtLocation(traverser, traverser.x1, traverser.y0, traverser.z1);
        foregroundVoxels += getAtLocation(traverser, traverser.x1, traverser.y1, traverser.z1);
        foregroundVoxels += getAtLocation(traverser, traverser.x0, traverser.y1, traverser.z1);
        return foregroundVoxels;
    }

    /**
     * Count the foreground voxels on the edges lining the stack
     * <p>
     * Contributes to χ_1 from Odgaard and Gundersen
     */
    public static <B extends BooleanType<B>> long stackEdges(final Traverser<B> traverser) {
        final long[] foregroundVoxels = {0};

        // left to right stack edges
        LongStream.of(traverser.z0, traverser.z1).forEach(z -> {
            LongStream.of(traverser.y0, traverser.y1).forEach(y -> {
                for (long x = 1; x < traverser.x1; x++) {
                    foregroundVoxels[0] += getAtLocation(traverser, x, y, z);
                }
            });
        });

        LongStream.of(traverser.z0, traverser.z1).forEach(z -> {
            LongStream.of(traverser.x0, traverser.x1).forEach(x -> {
                for (long y = 1; y < traverser.y1; y++) {
                    foregroundVoxels[0] += getAtLocation(traverser, x, y, z);
                }
            });
        });

        LongStream.of(traverser.y0, traverser.y1).forEach(y -> {
            LongStream.of(traverser.x0, traverser.x1).forEach(x -> {
                for (long z = 1; z < traverser.z1; z++) {
                    foregroundVoxels[0] += getAtLocation(traverser, x, y, z);
                }
            });
        });

        return foregroundVoxels[0];
    }

    /**
     * Count the foreground voxels on the faces that line the stack
     * <p>
     * Contributes to χ_2 from Odgaard and Gundersen
     */
    public static <B extends BooleanType<B>> int stackFaces(final Traverser<B> traverser) {
        final int[] foregroundVoxels = {0};

        LongStream.of(traverser.z0, traverser.z1).forEach(z -> {
            for (int y = 1; y < traverser.y1; y++) {
                for (int x = 1; x < traverser.x1; x++) {
                    foregroundVoxels[0] += getAtLocation(traverser, x, y, z);
                }
            }
        });

        LongStream.of(traverser.y0, traverser.y1).forEach(y -> {
            for (int z = 1; z < traverser.z1; z++) {
                for (int x = 1; x < traverser.x1; x++) {
                    foregroundVoxels[0] += getAtLocation(traverser, x, y, z);
                }
            }
        });

        LongStream.of(traverser.x0, traverser.x1).forEach(x -> {
            for (int y = 1; y < traverser.y1; y++) {
                for (int z = 1; z < traverser.z1; z++) {
                    foregroundVoxels[0] += getAtLocation(traverser, x, y, z);
                }
            }
        });

        return foregroundVoxels[0];
    }

    /**
     * Count the number of intersections between voxels in each 2x1 neighborhood and the the edges of the stack
     * <p>
     * Contributes to χ_1 from Odgaard and Gundersen
     * @implNote Public and static for testing purposes
     */
    public static <B extends BooleanType<B>> long voxelEdgeIntersections(final Traverser<B> traverser) {
        final int[] voxelVertices = {0};

        LongStream.of(traverser.z0, traverser.z1).forEach(z -> {
            traverser.access.setPosition(z, 2);
            LongStream.of(traverser.y0, traverser.y1).forEach(y -> {
                traverser.access.setPosition(y, 1);
                for (long x = 1; x < traverser.xSize; x++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x - 1, y, z);
                    voxelVertices[0] += voxelA | voxelB;
                }
            });
        });

        LongStream.of(traverser.z0, traverser.z1).forEach(z -> {
            traverser.access.setPosition(z, 2);
            LongStream.of(traverser.x0, traverser.x1).forEach(x -> {
                traverser.access.setPosition(x, 0);
                for (long y = 1; y < traverser.ySize; y++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x, y - 1, z);
                    voxelVertices[0] += voxelA | voxelB;
                }
            });
        });

        LongStream.of(traverser.y0, traverser.y1).forEach(y -> {
            traverser.access.setPosition(y, 1);
            LongStream.of(traverser.x0, traverser.x1).forEach(x -> {
                traverser.access.setPosition(x, 0);
                for (long z = 1; z < traverser.zSize; z++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x, y, z - 1);
                    voxelVertices[0] += voxelA | voxelB;
                }
            });
        });

        return voxelVertices[0];
    }

    /**
     * Count the intersections between voxel edges in each 2x2 neighborhood and the faces lining the stack
     * <p>
     * Contributes to χ_2 from Odgaard and Gundersen
     * @implNote Public and static for testing purposes
     */
    public static <B extends BooleanType<B>> long voxelEdgeFaceIntersections(final Traverser<B> traverser) {
        final long[] voxelEdges = {0};
        final long[] iterations = {0};

        // Front and back faces (all 4 edges). Check 2 edges per voxel
        LongStream.of(traverser.z0, traverser.z1).forEach(z -> {
            for (int y = 0; y <= traverser.ySize; y++) {
                for (int x = 0; x <= traverser.xSize; x++) {
                    final int voxel = getAtLocation(traverser, x, y, z);
                    if (voxel > 0) {
                        iterations[0]++;
                        voxelEdges[0] += 2;
                        continue;
                    }

                    voxelEdges[0] += getAtLocation(traverser, x, y - 1, z);
                    voxelEdges[0] += getAtLocation(traverser, x - 1, y, z);
                }
            }
        });

        // Top and bottom faces (horizontal edges)
        LongStream.of(traverser.y0, traverser.y1).forEach(y -> {
            for (int x = 0; x < traverser.xSize; x++) {
                for (int z = 1; z < traverser.zSize; z++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x, y, z - 1);
                    voxelEdges[0] += voxelA | voxelB;
                }
            }
        });

        // Top and bottom faces (vertical edges)
        LongStream.of(traverser.y0, traverser.y1).forEach(y -> {
            traverser.access.setPosition(y, 1);
            for (int z = 0; z < traverser.zSize; z++) {
                traverser.access.setPosition(z, 2);
                for (int x = 0; x <= traverser.xSize; x++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x - 1, y, z);
                    voxelEdges[0] += voxelA | voxelB;
                }
            }
        });

        // Left and right faces (horizontal edges)
        LongStream.of(traverser.x0, traverser.x1).forEach(x -> {
            traverser.access.setPosition(x, 0);
            for (int y = 0; y < traverser.ySize; y++) {
                traverser.access.setPosition(y, 1);
                for (int z = 1; z < traverser.zSize; z++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x, y, z - 1);
                    voxelEdges[0] += voxelA | voxelB;
                }
            }
        });

        // Left and right faces (vertical edges)
        LongStream.of(traverser.x0, traverser.x1).forEach(x -> {
            traverser.access.setPosition(x, 0);
            for (int z = 0; z < traverser.zSize; z++) {
                traverser.access.setPosition(z, 2);
                for (int y = 1; y < traverser.ySize; y++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x, y - 1, z);
                    voxelEdges[0] += voxelA | voxelB;
                }
            }
        });

        return voxelEdges[0];
    }

    /**
     * Count the intersections between voxels in each 2x2 neighborhood and the faces lining the stack
     * <p>
     * Contributes to χ_2 from Odgaard and Gundersen
     * @implNote Public and static for testing purposes
     */
    public static <B extends BooleanType<B>> long voxelFaceIntersections(final Traverser<B> traverser) {
        final long[] voxelFaces = {0};

        LongStream.of(traverser.z0, traverser.z1).forEach(z -> {
            traverser.access.setPosition(z, 2);
            for (int y = 0; y <= traverser.ySize; y++) {
                for (int x = 0; x <= traverser.xSize; x++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x - 1, y, z);
                    final int voxelC = getAtLocation(traverser, x, y - 1, z);
                    final int voxelD = getAtLocation(traverser, x - 1, y - 1, z);
                    voxelFaces[0] += voxelA | voxelB | voxelC | voxelD;
                }
            }
        });

        LongStream.of(traverser.x0, traverser.x1).forEach(x -> {
            traverser.access.setPosition(x, 0);
            for (int y = 0; y <= traverser.ySize; y++) {
                for (int z = 1; z < traverser.zSize; z++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x, y - 1, z);
                    final int voxelC = getAtLocation(traverser, x, y, z - 1);
                    final int voxelD = getAtLocation(traverser, x, y - 1, z - 1);
                    voxelFaces[0] += voxelA | voxelB | voxelC | voxelD;
                }
            }
        });

        LongStream.of(traverser.y0, traverser.y1).forEach(y -> {
            for (int x = 1; x < traverser.xSize; x++) {
                for (int z = 1; z < traverser.zSize; z++) {
                    final int voxelA = getAtLocation(traverser, x, y, z);
                    final int voxelB = getAtLocation(traverser, x, y, z - 1);
                    final int voxelC = getAtLocation(traverser, x - 1, y, z);
                    final int voxelD = getAtLocation(traverser, x - 1, y, z - 1);
                    voxelFaces[0] += voxelA | voxelB | voxelC | voxelD;
                }
            }
        });

        return voxelFaces[0];
    }

    //region -- Helper methods --
    private static <B extends BooleanType<B>> int getAtLocation(final Traverser<B> traverser, final long x,
                                                                final long y, final long z) {
        traverser.access.setPosition(x, 0);
        traverser.access.setPosition(y, 1);
        traverser.access.setPosition(z, 2);
        final double realDouble = traverser.access.get().getRealDouble();

        return (int) realDouble;
    }
    //endregion

    /** A convenience class for passing parameters */
    public static class Traverser<B extends BooleanType<B>> {
        public final long x0 = 0;
        public final long y0 = 0;
        public final long z0 = 0;
        public final long x1;
        public final long y1;
        public final long z1;
        public final long xSize;
        public final long ySize;
        public final long zSize;
        public final RandomAccess<B> access;

        public Traverser(RandomAccessibleInterval<B> interval) {
            xSize = interval.dimension(0);
            ySize = interval.dimension(1);
            zSize = interval.dimension(2);
            x1 = xSize - 1;
            y1 = ySize - 1;
            z1 = zSize - 1;
            access = Views.extendZero(interval).randomAccess();
        }
    }
}
