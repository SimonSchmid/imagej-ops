/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, University of Konstanz and Brian Northan.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops.threshold.localMean;

import net.imagej.ops.Ops;
import net.imagej.ops.map.neighborhood.AbstractCenterAwareComputerOp;
import net.imagej.ops.special.computer.Computers;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imagej.ops.threshold.LocalThresholdMethod;
import net.imagej.ops.threshold.apply.LocalThreshold;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * LocalThresholdMethod that uses the mean and operates directly of RAIs.
 *
 * @author Jonathan Hale (University of Konstanz)
 * @author Martin Horn (University of Konstanz)
 * @author Stefan Helfrich (University of Konstanz)
 */
@Plugin(type = Ops.Threshold.LocalThresholdMean.class, priority = Priority.LOW_PRIORITY)
public class LocalThresholdMean<T extends RealType<T>> extends
	LocalThreshold<T>	implements Ops.Threshold.LocalThresholdMean
{

	@Parameter
	private double c;

	@Override
	public void initialize() {
		method = new LocalMeanComputer<>(c, Computers.unary(ops(),
			Ops.Stats.Mean.class, DoubleType.class, Views.iterable(in())));
		
		super.initialize();
	}

	public static class LocalMeanComputer<I extends RealType<I>> extends
		LocalThresholdMethod<I>
	{

		private double c;
		private UnaryComputerOp<Iterable<I>, DoubleType> meanOp;

		public LocalMeanComputer(double c,
			UnaryComputerOp<Iterable<I>, DoubleType> meanOp)
		{
			super();
			this.c = c;
			this.meanOp = meanOp;
		}

		@Override
		public void compute1(final Pair<I, Iterable<I>> input,
			final BitType output)
		{

			final DoubleType m = new DoubleType();

			meanOp.compute1(input.getB(), m);
			output.set(input.getA().getRealDouble() > m.getRealDouble() - c);
		}
	}

}
