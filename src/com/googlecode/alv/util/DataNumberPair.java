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
 * An immutable container class to pair an object and a number. This class is
 * useful if for example you want to save some data and associate a certain turn
 * number or simply the amount of that data with it.
 * <p>
 * Note: This class has a natural ordering that is inconsistent with equals.
 */
public final class DataNumberPair<T> implements Comparable<DataNumberPair<?>> {
    private final Pair<T, Integer> dataNumberPair;

    /**
     * Creates a data number pairing of a generic object and a number.
     * 
     * @param <T> The type of data to associate with the number
     * @param data
     *            The data to set.
     * @param number
     *            The number to set.
     * @return A pair made from the given arguments
     * @throws NullPointerException
     *             if data is {@code null}; if number is {@code null}
     */
    public static <T> DataNumberPair<T> of(
                                           final T data, final Integer number) {
        return new DataNumberPair<T>(data, number);
    }

    /**
     * @param data
     *            The data to set.
     * @param number
     *            The number to set.
     * @throws NullPointerException
     *             if data is {@code null}; if number is {@code null}
     */
    private DataNumberPair(
                           final T data, final Integer number) {
        dataNumberPair = Pair.of(data, number);
    }

    /**
     * @return The data.
     */
    public T getData() {
        return dataNumberPair.getVar1();
    }

    /**
     * @return The number.
     */
    public Integer getNumber() {
        return dataNumberPair.getVar2();
    }

    /**
     * @return The difference between the number of this DataNumber instance and
     *         the number of the given DataNumber object.
     * @see Comparable
     */
    public int compareTo(
                         final DataNumberPair<?> dataNumber) {
        return dataNumberPair.getVar2().compareTo(dataNumber.getNumber());
    }

    @Override
    public String toString() {
        return dataNumberPair.getVar1().toString() + ": " + dataNumberPair.getVar2();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o == this)
            return true;

        if (o == null)
            return false;

        if (o instanceof DataNumberPair<?>)
            return getData().equals(((DataNumberPair<?>) o).getData())
                   && getNumber().equals(((DataNumberPair<?>) o).getNumber());

        return false;
    }

    @Override
    public int hashCode() {
        int result = 1147;
        result = 31 * result + dataNumberPair.hashCode();

        return result;
    }
}
