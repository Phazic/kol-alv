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

package com.googlecode.logVisualizer.util;

import java.util.EmptyStackException;
import java.util.List;

/**
 * A simple implementation of a LIFO stack.
 * <p>
 * Adding {@code null} is not allowed.
 */
public final class Stack<E> {

    /**
     * @return A new Stack.
     */
    public static <E> Stack<E> newStack() {
        return new Stack<E>();
    }

    private Option<StackElement> first;

    private Stack() {
        first = Option.none();
    }

    /**
     * Pushes the given element onto this stack.
     * <p>
     * Adding {@code null} is not allowed.
     * 
     * @param element
     *            Pushes this element onto this stack.
     */
    public void push(
                     final E element) {
        if (isEmpty())
            first = Option.some(new StackElement(element, null));
        else
            first = Option.some(new StackElement(element, first.get()));
    }

    /**
     * Pulls the first element off of the stack and returns it.
     * 
     * @return The first element of this stack.
     * @throws EmptyStackException
     *             if the stack is empty
     */
    public E pop() {
        if (isEmpty())
            throw new EmptyStackException();

        final E tmp = first.get().getElement().get();
        first = first.get().getNext();

        return tmp;
    }

    /**
     * Returns the first element on the stack without removing it.
     * 
     * @return An Option object containing the first element of this stack or
     *         {@link Option#NONE} in case the stack is empty.
     */
    public Option<E> peek() {
        return isEmpty() ? Option.<E> none() : first.get().getElement();
    }

    /**
     * @return {@code true} if this stack is empty, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return !first.isSome();
    }

    /**
     * Returns a list of all elements currently in the stack, without removing
     * them from the stack.
     * 
     * @return A list of all elements currently in the stack, in the order that
     *         {@link #pop()} would return them (meaning the standard order of a
     *         LIFO collection).
     */
    public List<E> getAllElements() {
        final List<E> result = Lists.newArrayList();

        Option<StackElement> current = first;
        while (current.isSome()) {
            result.add(current.get().getElement().get());
            current = current.get().getNext();
        }

        return result;
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o == this)
            return true;

        if (o instanceof Stack<?>)
            return first.equals(((Stack<?>) o).first);

        return false;
    }

    @Override
    public int hashCode() {
        return 634 + first.hashCode();
    }

    private final class StackElement {
        private final Option<E> element;

        private final Option<StackElement> next;

        StackElement(
                     final E element, final StackElement next) {
            this.element = Option.some(element);
            this.next = next != null ? Option.some(next) : Option.<StackElement> none();
        }

        Option<E> getElement() {
            return element;
        }

        Option<StackElement> getNext() {
            return next;
        }

        @Override
        public boolean equals(
                              final Object o) {
            if (o == this)
                return true;

            if (o == null)
                return false;

            if (o instanceof Stack<?>.StackElement) {
                final Stack<?>.StackElement that = (Stack<?>.StackElement) o;

                return element.equals(that.getElement()) && next.equals(that.getNext());
            }

            return false;
        }

        @Override
        public int hashCode() {
            int result = 834;
            result = result * 31 + element.hashCode();
            result = result * 31 + next.hashCode();

            return result;
        }
    }
}
