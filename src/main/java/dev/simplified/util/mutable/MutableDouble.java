package dev.simplified.util.mutable;

import dev.simplified.annotations.EqualsAndHashCode;

/**
 * A mutable {@code double} wrapper.
 * <p>
 * Note that as {@code MutableDouble} does not extend {@link Double}, it is not treated by
 * {@link String#format} as a {@link Double} parameter.
 *
 * @see Double
 */
@EqualsAndHashCode(identity = EqualsAndHashCode.Identity.INSTANCE_OF)
public class MutableDouble extends Number implements Comparable<MutableDouble>, Mutable<Number> {

    /**
     * The mutable value.
     */
    private double value;

    /**
     * Constructs a new {@code MutableDouble} with the default value of zero.
     */
    public MutableDouble() { }

    /**
     * Constructs a new {@code MutableDouble} with the specified value.
     *
     * @param value the initial value to store
     */
    public MutableDouble(final double value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code MutableDouble} with the specified value.
     *
     * @param value the initial value to store, not null
     * @throws NullPointerException if the value is null
     */
    public MutableDouble(final Number value) {
        this.value = value.doubleValue();
    }

    /**
     * Constructs a new {@code MutableDouble} by parsing the given string.
     *
     * @param value the string to parse, not null
     * @throws NumberFormatException if the string cannot be parsed into a double
     */
    public MutableDouble(final String value) {
        this.value = Double.parseDouble(value);
    }

    /**
     * Returns the value as a {@link Double} instance.
     *
     * @return the value as a {@link Double}, never null
     */
    @Override
    public Double get() {
        return this.value;
    }

    /**
     * Sets the value from a primitive {@code double}.
     *
     * @param value the value to set
     */
    public void set(final double value) {
        this.value = value;
    }

    /**
     * Sets the value from a {@link Number} instance.
     *
     * @param value the value to set, not null
     * @throws NullPointerException if the value is null
     */
    @Override
    public void set(final Number value) {
        this.value = value.doubleValue();
    }

    /**
     * Checks whether the double value is the special NaN value.
     *
     * @return {@code true} if the value is NaN
     */
    public boolean isNaN() {
        return Double.isNaN(value);
    }

    /**
     * Checks whether the double value is infinite.
     *
     * @return {@code true} if the value is infinite
     */
    public boolean isInfinite() {
        return Double.isInfinite(value);
    }

    /**
     * Increments the value by one.
     */
    public void increment() {
        value++;
    }

    /**
     * Returns the current value and then increments by one. This method is not thread safe.
     *
     * @return the value before incrementing
     */
    public double getAndIncrement() {
        final double last = value;
        value++;
        return last;
    }

    /**
     * Increments by one and then returns the new value. This method is not thread safe.
     *
     * @return the value after incrementing
     */
    public double incrementAndGet() {
        value++;
        return value;
    }

    /**
     * Decrements the value by one.
     */
    public void decrement() {
        value--;
    }

    /**
     * Returns the current value and then decrements by one. This method is not thread safe.
     *
     * @return the value before decrementing
     */
    public double getAndDecrement() {
        final double last = value;
        value--;
        return last;
    }

    /**
     * Decrements by one and then returns the new value. This method is not thread safe.
     *
     * @return the value after decrementing
     */
    public double decrementAndGet() {
        value--;
        return value;
    }

    /**
     * Adds the given operand to this instance's value.
     *
     * @param operand the value to add
     */
    public void add(final double operand) {
        this.value += operand;
    }

    /**
     * Adds the given operand to this instance's value.
     *
     * @param operand the value to add, not null
     * @throws NullPointerException if the operand is null
     */
    public void add(final Number operand) {
        this.value += operand.doubleValue();
    }

    /**
     * Subtracts the given operand from this instance's value.
     *
     * @param operand the value to subtract
     */
    public void subtract(final double operand) {
        this.value -= operand;
    }

    /**
     * Subtracts the given operand from this instance's value.
     *
     * @param operand the value to subtract, not null
     * @throws NullPointerException if the operand is null
     */
    public void subtract(final Number operand) {
        this.value -= operand.doubleValue();
    }

    /**
     * Adds the given operand and then returns the new value. This method is not thread safe.
     *
     * @param operand the quantity to add
     * @return the value after adding the operand
     */
    public double addAndGet(final double operand) {
        this.value += operand;
        return value;
    }

    /**
     * Adds the given operand and then returns the new value. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @throws NullPointerException if {@code operand} is null
     * @return the value after adding the operand
     */
    public double addAndGet(final Number operand) {
        this.value += operand.doubleValue();
        return value;
    }

    /**
     * Returns the current value and then adds the given operand. This method is not thread safe.
     *
     * @param operand the quantity to add
     * @return the value before adding the operand
     */
    public double getAndAdd(final double operand) {
        final double last = value;
        this.value += operand;
        return last;
    }

    /**
     * Returns the current value and then adds the given operand. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @throws NullPointerException if {@code operand} is null
     * @return the value before adding the operand
     */
    public double getAndAdd(final Number operand) {
        final double last = value;
        this.value += operand.doubleValue();
        return last;
    }

    /** {@inheritDoc} */
    @Override
    public int intValue() {
        return (int) value;
    }

    /** {@inheritDoc} */
    @Override
    public long longValue() {
        return (long) value;
    }

    /** {@inheritDoc} */
    @Override
    public float floatValue() {
        return (float) value;
    }

    /** {@inheritDoc} */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Converts this mutable to a {@link Double} instance.
     *
     * @return a {@link Double} containing this mutable's value, never null
     */
    public Double toDouble() {
        return doubleValue();
    }

    /**
     * Compares this mutable to another in ascending order.
     *
     * @param other the other mutable to compare to, not null
     * @return negative if this is less, zero if equal, positive if greater
     */
    @Override
    public int compareTo(final MutableDouble other) {
        return Double.compare(this.value, other.value);
    }

    /**
     * Returns the string representation of this mutable's value.
     *
     * @return the mutable value as a string
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
