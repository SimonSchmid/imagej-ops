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

import com.googlecode.gentyref.GenericTypeReflector;
import com.googlecode.gentyref.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import net.imagej.ImageJService;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.GenericByteType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.service.AbstractService;
import org.scijava.util.ClassUtils;
import org.scijava.util.GenericUtils;

/**
 * Interface for services that FIXME
 * 
 * @author Curtis Rueden
 */
public class Types extends AbstractService implements ImageJService {

	public static <T extends GenericByteType<T>> void go() {
		Type exactType = exactType("field", Test1.class, Test1.class);
		Type tokenType = new TypeToken<Vague<T, Img<T>>>() {}.getType();
		System.out.println("* exact = " + exactType);
		System.out.println("* token = " + tokenType);
//		new RealThing<T>().fieldToMatch = new Specific();
		System.out.println("DOES IT WORK? " + fitsInto(Specific.class, exactType));
	}

	private static boolean fitsInto(final Object obj, Type destType) {
//		return fitsInto(genericType(obj), destType);
		return fitsInto(obj.getClass(), destType); // obviously insufficient
		// is not good enough, because...
	}

	private static boolean fitsInto(final Type objType, Type destType) {
		// CTR: Unfortunately, this method does not behave as hoped.
		return GenericTypeReflector.isSuperType(destType, objType);
	}

	////////////////////////////////

	public static class Vague<T, I extends IterableInterval<T> & RandomAccessibleInterval<T>> {
		//
	}
	public static class Specific extends Vague<ByteType, Img<ByteType>> {
		//
	}

	public static class Test1<T, I extends IterableInterval<T> & RandomAccessibleInterval<T>> {
		public Vague<T, I> field;
	}

	public static void main(final String[] args) {
		// test with type
		go();

		// test with object
		Specific obj = new Specific();
		new Test1<ByteType, Img<ByteType>>().field = obj;
		Type destType = exactType("field", Test1.class, Test1.class);
		System.out.println(fitsInto(obj, destType));
		if (true) return;

//		System.out.println();
//		System.out.println("== T extends RealType<T> as viewed by I extends IntegerType<I> ==");
//		Field field = ClassUtils.getField(RealThing.class, "realPix");
//		Type t = GenericTypeReflector.getExactFieldType(field, IntegerThing.class);
////		System.out.println("erased = " + GenericTypeReflector.erase(t));
//		WildcardType wc = (WildcardType) GenericTypeReflector
//			.getTypeParameter(t, RealType.class.getTypeParameters()[0]);
//		System.out.println("first RealType param = " + wc);
////		System.out.println(((ParameterizedType) t).getActualTypeArguments()[0].getClass().getName());
//
//		System.out.println();
//		System.out.println("== RealThing<?> ==");
//		Type type = GenericTypeReflector.addWildcardParameters(RealThing.class);
//		System.out.println(type.getTypeName());
//		if (type instanceof ParameterizedType) {
//			ParameterizedType pt = (ParameterizedType) type;
//			for (Type arg : pt.getActualTypeArguments()) {
//				// need to iterate all upper and lower bounds
//				((WildcardType) arg).getLowerBounds();
//				System.out.println("=> " + arg.getTypeName() + " [" + arg.getClass() + "]");
//			}
//		}
	}

	private static void dump(String fieldName, Class<?> fieldClass, Class<?> viewClass, Class<?> superClass) {
		System.out.println("Field " + fieldClass.getName() + "." + fieldName + ":");

		// This step is already done in ModuleItem.getGenericType()
		Type exactType = exactType(fieldName, fieldClass, viewClass);

		System.out.println("=> exact type (viewed through " + viewClass.getName() + ") = " + exactType.getTypeName());

		// Now we want to ask: for this generic type: what does it look like
		// with respect to one of its super types?
		// For example, if FloatImg extends ArrayImg<FloatType>, and you ask
		// for getExactSuperType(FloatImg, Img), you'll get Img<FloatType>.
		Type type = GenericTypeReflector.getExactSuperType(exactType, superClass);
		
		// If this returns null, then superClass is not a supertype of the type
		// in question, in which case we do not have a real match.

		// If this returns a Class, then supertype has no type parameter for superClass.
		// But (in our case at least?), this should not happen if superClass has type parameters.
		// Let's check for that scenario and throw an exception in that case.
		// i.e.: superClass.getTypeParameters().length == ((ParameterizedType) type).getActualTypeArguments().length

		

		// - need to recurse as appropriate
		// - 

		// Now that we have the exact super type, 
		System.out.println("Exact supertype of " + superClass.getName() + ": " + type.getTypeName());

		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			for (Type arg : pt.getActualTypeArguments()) {
				System.out.println("=> " + arg.getTypeName());
			}
		}
		System.out.println();
	}

	private static Type exactType(String fieldName, Class<?> fieldClass,
		Class<?> viewClass)
	{
		Field field = ClassUtils.getField(fieldClass, fieldName);
		Type exactType = GenericUtils.getFieldType(field, viewClass);
		return exactType;
	}
}