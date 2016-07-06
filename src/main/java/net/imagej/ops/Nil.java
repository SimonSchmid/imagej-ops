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

package net.imagej.ops;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Set;

import org.scijava.util.ConversionUtils;

/**
 * A "typed null" which knows its generic type, and can generate proxy objects
 * implementing that type's interfaces, with customizable behavior per interface
 * method via callbacks.
 * 
 * @author Curtis Rueden
 */
public abstract class Nil<T> implements GenericTyped, GenericProxy<T>,
	InvocationHandler
{

	/** Generic type of the object. */
	private TypeToken<T> typeToken = new TypeToken<T>(getClass()) {
		// NB: No implementation needed.
	};

	// -- GenericTyped methods --

	@Override
	public Type getType() { return typeToken.getType(); }

	// -- GenericProxy methods --

	/**
	 * Create a proxy which implements the all same interfaces as this object's
	 * generic type.
	 * <p>
	 * CTR FIXME - write up how method delegation works.
	 * </p>
	 */
	@Override
	public T proxy() {
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();

		// extract the generic type's interfaces
		final Set<Class<? super T>> ifaceSet = //
			typeToken.getTypes().interfaces().rawTypes();
		final boolean appendGT = !ifaceSet.contains(GenericTyped.class);
		final int ifaceCount = ifaceSet.size() + (appendGT ? 1 : 0);
		final Class<?>[] interfaces = new Class<?>[ifaceCount];
		ifaceSet.toArray(interfaces);
		if (appendGT) interfaces[ifaceCount - 1] = GenericTyped.class;

		@SuppressWarnings("unchecked")
		final T proxy = (T) Proxy.newProxyInstance(loader, interfaces, this);
		return proxy;
	}

	// -- InvocationHandler methods --

	@Override
	public Object invoke(final Object proxy, final Method method,
		final Object[] args) throws Throwable
	{
		try {
			// Look for a Nil subclass method of the same signature.
			final Method m = getClass().getMethod(method.getName(),
				method.getParameterTypes());
			return m.invoke(Nil.super, args);
		}
		catch (final NoSuchMethodException exc) {
			// NB: Default behavior is to do nothing and return null.
			return ConversionUtils.getNullValue(method.getReturnType());
		}
	}

}
