package dev.simplified.util.mutable;

import dev.simplified.annotations.EqualsAndHashCode;

/**
 * A mutable {@code float} wrapper.
 * <p>
 * Note that as {@code MutableFloat} does not extend {@link Float}, it is not treated by
 * {@link String#format} as a {@link Float} parameter.
 *
 * @see Float
 */
@EqualsAndHashCode(identity = EqualsAndHashCode.Identity.INSTANCE_OF)
public class MutableFloat extends Number implements Comparable<MutableFloat>, Mutable<Number> {

    /**
     * The mutable value.
     */
    private float value;

    /**
     * Constructs a new {@code MutableFloat} with the default value of zero.
     */
    public MutableFloat() { }

    /**
     * Constructs a new {@code MutableFloat} with the specified value.
     *
     * @param value the initial value to store
     */
    public MutableFloat(final float value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code MutableFloat} with the specified value.
     *
     * @param value the initial value to store, not null
     * @throws NullPointerException if the value is null
     */
    public MutableFloat(final Number value) {
        this.value = value.floatValue();
    }

    /**
     * Constructs a new {@code MutableFloat} by parsing the given string.
     *
     * @param value the string to parse, not null
     * @throws NumberFormatException if the string cannot be parsed into a float
     */
    public MutableFloat(final String value) {
        this.value = Float.parseFloat(value);
    }

    /**
     * Returns the value as a {@link Float} instance.
     *
     * @return the value as a {@link Float}, never null
     */
    @Override
    public Float get() {
        return this.value;
    }

    /**
     * Sets the value from a primitive {@code float}.
     *
     * @param value the value to set
     */
    public void set(final float value) {
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
        this.value = value.floatValue();
    }

    /**
     * Checks whether the float value is the special NaN value.
     *
     * @return {@code true} if the value is NaN
     */
    public boolean isNaN() {
        return Float.isNaN(value);
    }

    /**
     * Checks whether the float value is infinite.
     *
     * @return {@code true} if the value is infinite
     */
    public boolean isInfinite() {
        return Float.isInfinite(value);
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
    public float getAndIncrement() {
        final float last = value;
        value++;
        return last;
    }

    /**
     * Increments by one and then returns the new value. This method is not thread safe.
     *
     * @return the value after incrementing
     */
    public float incrementAndGet() {
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
    public float getAndDecrement() {
        final float last = value;
        value--;
        return last;
    }

    /**
     * Decrements this instance's value by 1; this method returns the value associated with the instance
     * immediately after the decrement operation. This method is not thread safe.
     *
     * @return the value associated with the instance after it is decremented
     */
    public float decrementAndGet() {
        value--;
        return value;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a value to the value of this instance.
     *
     * @param operand the value to add, not null
     */
    public void add(final float operand) {
        this.value += operand;
    }

    /**
     * Adds a value to the value of this instance.
     *
     * @param operand the value to add, not null
     * @throws NullPointerException if the object is null
     */
    public void add(final Number operand) {
        this.value += operand.floatValue();
    }

    /**
     * Subtracts a value from the value of this instance.
     *
     * @param operand the value to subtract
     */
    public void subtract(final float operand) {
        this.value -= operand;
    }

    /**
     * Subtracts a value from the value of this instance.
     *
     * @param operand the value to subtract, not null
     * @throws NullPointerException if the object is null
     */
    public void subtract(final Number operand) {
        this.value -= operand.floatValue();
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated with the instance
     * immediately after the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @return the value associated with this instance after adding the operand
     */
    public float addAndGet(final float operand) {
        this.value += operand;
        return value;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated with the instance
     * immediately after the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @throws NullPointerException if {@code operand} is null
     * @return the value associated with this instance after adding the operand
     */
    public float addAndGet(final Number operand) {
        this.value += operand.floatValue();
        return value;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated with the instance
     * immediately prior to the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @return the value associated with this instance immediately before the operand was added
     */
    public float getAndAdd(final float operand) {
        final float last = value;
        this.value += operand;
        return last;
    }

    /**
     * Increments this instance's value by {@code operand}; this method returns the value associated with the instance
     * immediately prior to the addition operation. This method is not thread safe.
     *
     * @param operand the quantity to add, not null
     * @throws NullPointerException if {@code operand} is null
     * @return the value associated with this instance immediately before the operand was added
     */
    public float getAndAdd(final Number operand) {
        final float last = value;
        this.value += operand.floatValue();
        return last;
    }

    //-----------------------------------------------------------------------
    // shortValue and byteValue rely on Number implementation
    /**
     * Returns the value of this MutableFloat as an int.
     *
     * @return the numeric value represented by this object after conversion to type int
     */
    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Returns the value of this MutableFloat as a long.
     *
     * @return the numeric value represented by this object after conversion to type long
     */
    @Override
    public long longValue() {
        return (long) value;
    }

    /**
     * Returns the value of this MutableFloat as a float.
     *
     * @return the numeric value represented by this object after conversion to type float
     */
    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the value of this MutableFloat as a double.
     *
     * @return the numeric value represented by this object after conversion to type double
     */
    @Override
    public double doubleValue() {
        return value;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets this mutable as an instance of Float.
     *
     * @return a Float instance containing the value from this mutable, never null
     */
    public Float toFloat() {
        return floatValue();
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this mutable to another in ascending order.
     *
     * @param other the other mutable to compare to, not null
     * @return negative if this is less, zero if equal, positive if greater
     */
    @Override
    public int compareTo(final MutableFloat other) {
        return Float.compare(this.value, other.value);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the String value of this mutable.
     *
     * @return the mutable value as a string
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
