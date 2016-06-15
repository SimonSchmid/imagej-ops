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
package net.imagej.ops.features.sets;

import java.util.List;

import net.imagej.ops.Op;
import net.imagej.ops.special.computer.Computers;

/**
 * A {@link ConfigurableComputerSet} can be initialized with only a subset of
 * activated {@link Computers}.
 *
 * @author Tim-Oliver Buchholz, University of Konstanz
 *
 * @param <I>
 *            type of the common input
 * @param <O>
 *            type of the common output
 */
public interface ConfigurableComputerSet<I, O> extends ComputerSet<I, O> {

	/**
	 * Get the activation state of a {@link Computers} of this
	 * {@link ConfigurableComputerSet}.
	 *
	 * @param computer
	 *            the {@link Computers} to check.
	 * @return the activation state of this {@link Computers}
	 */
	boolean isActive(final Class<? extends Op> computer);

	/**
	 * Get all active {@link Computers} of this {@link ConfigurableComputerSet}.
	 *
	 * @return the active {@link Computers}
	 */
	List<Class<? extends Op>> getActive();

	/**
	 * Get all inactive {@link Computers} of this
	 * {@link ConfigurableComputerSet}.
	 *
	 * @return the inactive {@link Computers}
	 */
	List<Class<? extends Op>> getInactive();

}
