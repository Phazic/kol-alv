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
 * An immutable container class to save two objects which thus are linked
 * together. In other words, a tuple.
 */
public final class Pair<T, U> {
    private final T var1;

    private final U var2;

    /**
     * Creates a pair of the two given objects.
     * 
     * @param <T> The type of the first element of the pair
     * @param <U> The type of the second element of the pair
     * @param var1
     *            The first value to set.
     * @param var2
     *            The second value to set.
     * @return The newly created pair
     * @throws NullPointerException
     *             if var1 is {@code null}; if var2 is {@code null}
     */
    public static <T, U> Pair<T, U> of(
                                       final T var1, final U var2) {
        return new Pair<T, U>(var1, var2);
    }

    /**
     * @param var1
     *            The first value to set.
     * @param var2
     *            The second value to set.
     * @throws NullPointerException
     *             if var1 is {@code null}; if var2 is {@code null}
     */
    private Pair(
                 final T var1, final U var2) {
        if (var1 == null)
            throw new NullPointerException("The key cannot be null.");
        if (var2 == null)
            throw new NullPointerException("The value cannot be null.");

        this.var1 = var1;
        this.var2 = var2;
    }

    /**
     * @return The first value.
     */
    public final T getVar1() {
        return var1;
    }

    /**
     * @return The second value.
     */
    public final U getVar2() {
        return var2;
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o == this)
            return true;

        if (o == null)
            return false;

        if (o instanceof Pair<?, ?>)
            return var1.equals(((Pair<?, ?>) o).getVar1())
                   && var2.equals(((Pair<?, ?>) o).getVar2());

        return false;
    }

    @Override
    public int hashCode() {
        int result = 4711;
        result = 31 * result + var1.hashCode();
        result = 31 * result + var2.hashCode();

        return result;
    }
}
