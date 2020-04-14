/* Copyright (c) 2008-2011, developers of the Ascension Log Visualizer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.googlecode.alv.util;

/**
 * An implementation of an Option class as they can be often found in functional
 * programming languages such as Scala or Haskell.
 * <p>
 * The idea is to use a container class to be able to differentiate between data
 * and no data without having to use {@code null}.
 * <p>
 * In case of data being there, the static method {@link Option#some(Object)}
 * should be used to create an Option object with that data. In case of no data,
 * the static method {@link Option#none()} should be used, which returns the
 * static final field {@link Option#NONE} in a type-safe way.
 */
public class Option<E> {
    private final E element;

    /**
     * Use this object when referring to no data. Usual idiomatic Java would
     * probably use {@code null} in such cases.
     * <p>
     * If you call {@link Option#get()} on this object, an
     * {@link UnsupportedOperationException} will be thrown.
     */
    public static final Option<?> NONE = new Option<Object>(null) {
        @Override
        public Object get() {
            throw new UnsupportedOperationException("The None-Option doesn't have an element.");
        }
    };

    /**
     * Creates an Option instance containing the given data.
     * 
     * @param <T> The type of element for which to make an Option
     * @param element
     *            The data contained inside the Option.
     * @throws IllegalArgumentException
     *             if the element is {@code null}
     * @return The created Option instance with the given data.
     */
    public static <T> Option<T> some(
                                     final T element) {
        if (element == null)
            throw new IllegalArgumentException("The element must not be null.");

        return new Option<T>(element);
    }

    /**
     * Returns the {@link Option#NONE} object in a more type-safe way.
     * 
     * @param <T> The type of Option
     * @return An Option instance containing no data. Is equal with
     *         {@link Option#NONE}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Option<T> none() {
        return (Option<T>) NONE;
    }

    private Option(
                   final E element) {
        this.element = element;
    }

    /**
     * @return The data contained inside this Option instance.
     */
    public E get() {
        return element;
    }

    /**
     * @return True in case this Option instance contains data, otherwise false.
     */
    public boolean isSome() {
        return element != null;
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o == this)
            return true;

        if (o == NONE)
            return false;

        if (o == null)
            return false;

        if (o instanceof Option<?>)
            return ((Option<?>) o).get().equals(element);

        return false;
    }

    @Override
    public int hashCode() {
        return this == NONE ? 767 : 767 * 31 + element.hashCode();
    }
}
