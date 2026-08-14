package dev.simplified.util.mutable;

import dev.simplified.annotations.AllArgsConstructor;
import dev.simplified.annotations.EqualsAndHashCode;
import dev.simplified.annotations.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * A generic interface providing mutable access to a value.
 * <p>
 * A typical use case is to pass a primitive or string to a method and allow that method to
 * effectively change the value. Another use case is to store a frequently changing primitive in
 * a collection (for example a total in a map) without creating new wrapper objects.
 *
 * @param <T> the type to set and get
 */
public interface Mutable<T> {

    /**
     * The stored value of this mutable.
     */
    T get();

    /**
     * Sets the value of this mutable.
     *
     * @param value the value to store
     * @throws NullPointerException if the value is null and null is not permitted
     * @throws ClassCastException if the value type is incompatible
     */
    void set(T value);

    /**
     * Creates a new {@link Mutable} with a {@code null} initial value.
     *
     * @param <T> the type of the value to be stored
     * @return a new mutable instance
     */
    static <T> @NotNull Mutable<T> of() {
        return new Impl<>();
    }

    /**
     * Creates a new {@link Mutable} with the given initial value.
     *
     * @param <T> the type of the value to be stored
     * @param value the initial value, may be null
     * @return a new mutable instance containing the specified value
     */
    static <T> @NotNull Mutable<T> of(@Nullable T value) {
        return new Impl<>(value);
    }

    /**
     * A mutable {@link Object} wrapper implementing {@link Mutable} and {@link Serializable}.
     *
     * @param <T> the type to set and get
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    class Impl<T> implements Mutable<T>, Serializable {

        private @Nullable T value;

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public void set(final T value) {
            this.value = value;
        }

        /**
         * Returns the string representation of the stored value, or {@code "null"} if the value is {@code null}.
         *
         * @return the string form of this mutable's value
         */
        @Override
        public String toString() {
            return this.value == null ? "null" : this.value.toString();
        }

    }

}
