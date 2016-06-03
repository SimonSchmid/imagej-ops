package net.imagej.ops.features.hog;

import org.scijava.plugin.Plugin;

import net.imagej.ops.AbstractNamespace;
import net.imagej.ops.Namespace;

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
	// @OpMethod(op =
	// net.imagej.ops.features.hog.HistogramOfOrientedGradients2D.class)
	// public <T extends RealType<T>> RandomAccessibleInterval<T> hog(final
	// RandomAccessibleInterval<T> in,
	// final RandomAccessibleInterval<T> out) {
	// @SuppressWarnings("unchecked")
	// final RandomAccessibleInterval<T> result = (RandomAccessibleInterval<T>)
	// ops()
	// .run(Ops.HoG.HistogramOfOrientedGradients.class, in, out);
	// return result;
	// }
}
