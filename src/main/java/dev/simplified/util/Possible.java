package dev.simplified.util;

import dev.simplified.annotations.EqualsAndHashCode;
import dev.simplified.annotations.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A container object which may contain a non-{@code null} value, and which tells apart two ways of
 * holding none. It is {@link Optional} with one more state: every method the two share answers
 * exactly as {@link Optional} would, and the extra state is reached only through methods
 * {@link Optional} does not have.
 * <p>
 * The three states, as {@link #getState()} reports them:
 * <ul>
 *     <li><b>{@link State#PRESENT present}</b> - the container exists and holds a value</li>
 *     <li><b>{@link State#EMPTY empty}</b> - the container exists and holds nothing: zero elements,
 *     or a {@code null}</li>
 *     <li><b>{@link State#ABSENT absent}</b> - there is no container at all; the value is
 *     structurally missing</li>
 * </ul>
 * <p>
 * Absent is the stronger form of empty, so {@link #isEmpty()} answers {@code true} for both - as
 * {@link Optional#isEmpty()} does for the {@link Optional} each converts to - and
 * {@link #isAbsent()} singles out the stronger one. A map lookup is the clearest case: a key bound to
 * {@code null} answers empty, and a key the map does not hold answers absent, where {@link Optional}
 * collapses both onto {@link Optional#empty()} and leaves the caller to ask a second question.
 * <p>
 * Code written against {@link Optional} therefore keeps its meaning when the type is replaced, and an
 * absent value reaching it is treated as the empty one it also is. A caller that needs all three
 * states switches over {@link #getState()} in a switch <i>expression</i>, which the compiler checks
 * for exhaustiveness; a switch statement over an enum is not checked.
 *
 * @apiNote
 * {@code Possible} is primarily intended for use as a method return type where "no result" has more
 * than one meaning and the caller must be able to act on which one it was, and where using
 * {@code null} to encode the second meaning is likely to cause errors. A variable whose type is
 * {@code Possible} should never itself be {@code null}; it should always point to a {@code Possible}
 * instance.
 *
 * @apiNote
 * {@link #equals(Object)} is the one shared method that does not follow {@link Optional}: an empty and
 * an absent {@code Possible} are unequal, although both convert to {@link Optional#empty()}. Telling
 * them apart is what the type is for.
 *
 * @param <T> the type of value
 */
@EqualsAndHashCode
public final class Possible<T> {

    /**
     * Common instance for {@link #empty()}.
     */
    private static final Possible<?> EMPTY = new Possible<>(State.EMPTY, null);

    /**
     * Common instance for {@link #absent()}.
     */
    private static final Possible<?> ABSENT = new Possible<>(State.ABSENT, null);

    /**
     * Which of the three states this instance holds. It is compared, so an empty and an absent
     * instance are unequal despite both carrying no value.
     */
    @Getter
    private final @NotNull State state;

    /**
     * The wrapped value; {@code null} in both value-less states.
     */
    private final @Nullable T value;

    private Possible(@NotNull State state, @Nullable T value) {
        this.state = state;
        this.value = value;
    }

    /**
     * The three states a {@code Possible} can be in, from holding a value to having no container.
     */
    public enum State {

        /** The container exists and holds a value. */
        PRESENT,

        /** The container exists and holds nothing - zero elements, or a {@code null}. */
        EMPTY,

        /** There is no container, and the value is structurally missing - the stronger form of empty. */
        ABSENT

    }

    // Create

    /**
     * Returns an empty {@code Possible} instance. No value is present, and the container exists but
     * holds nothing.
     *
     * @apiNote
     * Avoid testing emptiness by comparing with {@code ==} against instances returned by
     * {@code Possible.empty()}. There is no guarantee it is a singleton. Use {@link #isEmpty()} or
     * {@link #isPresent()} instead.
     *
     * @param <T> the type of the non-existent value
     * @return an empty {@code Possible}
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull Possible<T> empty() {
        return (Possible<T>) EMPTY;
    }

    /**
     * Returns an absent {@code Possible} instance. No value is present, and there is no container to
     * hold one.
     *
     * @apiNote
     * Avoid testing absence by comparing with {@code ==} against instances returned by
     * {@code Possible.absent()}. There is no guarantee it is a singleton. Use {@link #isAbsent()}
     * instead.
     *
     * @param <T> the type of the non-existent value
     * @return an absent {@code Possible}
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull Possible<T> absent() {
        return (Possible<T>) ABSENT;
    }

    /**
     * Returns a {@code Possible} describing the given non-{@code null} value.
     *
     * @param value the value to describe, must be non-{@code null}
     * @param <T> the type of the value
     * @return a {@code Possible} with the value present
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static <T> @NotNull Possible<T> of(@NotNull T value) {
        if (value == null)
            throw new NullPointerException("A present Possible carries a value");

        return new Possible<>(State.PRESENT, value);
    }

    /**
     * Returns a {@code Possible} describing the given value, or an empty {@code Possible} if it is
     * {@code null}.
     *
     * @apiNote
     * The {@code null} maps to empty rather than absent, because a {@code null} is something held that
     * holds nothing, and says nothing about whether a container was missing. A caller that means
     * absent says so with {@link #absent()}.
     *
     * @param value the possibly-{@code null} value to describe
     * @param <T> the type of the value
     * @return a {@code Possible} with the value present if it is non-{@code null}, otherwise an empty
     *         {@code Possible}
     */
    public static <T> @NotNull Possible<T> ofNullable(@Nullable T value) {
        return value == null ? empty() : new Possible<>(State.PRESENT, value);
    }

    /**
     * Returns a {@code Possible} describing the value contained in the given {@link Optional}, or an
     * empty {@code Possible} if the {@code Optional} is empty.
     *
     * @apiNote
     * An empty {@link Optional} maps to empty rather than absent, which is the faithful reading:
     * {@link Optional} cannot express absence, so it never carries one to recover. A caller who knows
     * the empty meant absence converts it with {@code or(Possible::absent)}, since the result is never
     * absent to begin with.
     *
     * @param value a non-null {@code Optional} that may or may not contain a value
     * @param <T> the type of the value
     * @return a {@code Possible} with the value present if the {@code Optional} contains one,
     *         otherwise an empty {@code Possible}
     */
    public static <T> @NotNull Possible<T> ofOptional(@NotNull Optional<? extends T> value) {
        return value.<Possible<T>>map(Possible::of).orElseGet(Possible::empty);
    }

    // Get

    /**
     * If a value is present, returns the value, otherwise throws {@link NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Possible}
     * @throws NoSuchElementException if no value is present
     */
    public @NotNull T get() {
        if (this.value == null)
            throw new NoSuchElementException(this.state == State.ABSENT ? "Value absent" : "No value present");

        return this.value;
    }

    // Present / Empty / Absent

    /**
     * Returns {@code true} if a value is present, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return this.state == State.PRESENT;
    }

    /**
     * Returns {@code true} if no value is present, otherwise {@code false}. An absent
     * {@code Possible} is empty too - absent is the stronger form - so this answers as
     * {@link Optional#isEmpty()} does.
     *
     * @return {@code true} if no value is present, otherwise {@code false}
     */
    public boolean isEmpty() {
        return this.state != State.PRESENT;
    }

    /**
     * Returns {@code true} if there is no container at all, otherwise {@code false}.
     *
     * @apiNote
     * An absent {@code Possible} is also empty. {@code getState() == State.EMPTY} is what singles out
     * one that is empty and not absent.
     *
     * @return {@code true} if this is absent, otherwise {@code false}
     */
    public boolean isAbsent() {
        return this.state == State.ABSENT;
    }

    // IfPresent

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param action the action to be performed if a value is present
     */
    public void ifPresent(@NotNull Consumer<? super T> action) {
        if (this.value != null)
            action.accept(this.value);
    }

    /**
     * If a value is present, performs the given action with the value, otherwise performs the given
     * empty-based action.
     *
     * @param action the action to be performed if a value is present
     * @param emptyAction the action to be performed if no value is present, absent included
     */
    public void ifPresentOrElse(@NotNull Consumer<? super T> action, @NotNull Runnable emptyAction) {
        if (this.value != null)
            action.accept(this.value);
        else
            emptyAction.run();
    }

    // Filter

    /**
     * If a value is present and it matches the given predicate, returns this {@code Possible},
     * otherwise returns an empty {@code Possible}. A value-less {@code Possible} is returned as it is,
     * so filtering never turns an absent one into an empty one.
     *
     * @apiNote
     * A present value the predicate rejects yields empty rather than absent, because the container
     * existed and held a value to test - after filtering it holds nothing.
     *
     * @param predicate the predicate to apply to the value, if present
     * @return this {@code Possible} if a value is present and matches, otherwise empty
     */
    public @NotNull Possible<T> filter(@NotNull Predicate<? super T> predicate) {
        if (this.value == null) return this;
        return predicate.test(this.value) ? this : empty();
    }

    // Map

    /**
     * If a value is present, applies the given mapping function to it and returns a {@code Possible}
     * describing the result, otherwise returns a {@code Possible} in this one's state. If the mapping
     * function returns {@code null}, returns an empty {@code Possible}.
     * <p>
     * A value-less {@code Possible} keeps its reason across a mapping, so an absent one maps to an
     * absent one. That is what lets a caller postpone the decision about what absence means until
     * after a chain of transformations.
     *
     * @param mapper the mapping function to apply to the value, if present
     * @param <U> the type of the value returned from the mapping function
     * @return a {@code Possible} describing the mapped result, or this one's value-less state
     */
    @SuppressWarnings("unchecked")
    public <U> @NotNull Possible<U> map(@NotNull Function<? super T, ? extends U> mapper) {
        if (this.value == null) return (Possible<U>) this;
        return ofNullable(mapper.apply(this.value));
    }

    // FlatMap

    /**
     * If a value is present, applies the given {@code Possible}-bearing mapping function to it and
     * returns the result, otherwise returns a {@code Possible} in this one's state.
     * <p>
     * Unlike {@link #map(Function)}, the mapping function already returns a {@code Possible}, so
     * {@code flatMap} does not wrap it in an additional layer - and the mapper decides for itself
     * which value-less state its own result carries.
     *
     * @param mapper the mapping function to apply to the value, if present
     * @param <U> the type of the value of the {@code Possible} returned by the mapping function
     * @return the result of applying the mapping function, or this one's value-less state
     */
    @SuppressWarnings("unchecked")
    public <U> @NotNull Possible<U> flatMap(@NotNull Function<? super T, ? extends Possible<? extends U>> mapper) {
        if (this.value == null) return (Possible<U>) this;
        return (Possible<U>) mapper.apply(this.value);
    }

    // Or

    /**
     * If a value is present, returns this {@code Possible}, otherwise returns the {@code Possible}
     * produced by the supplying function. Both value-less states are replaced.
     *
     * @param supplier a function that produces a fallback {@code Possible}
     * @return this {@code Possible} if a value is present, otherwise the result of {@code supplier}
     */
    @SuppressWarnings("unchecked")
    public @NotNull Possible<T> or(@NotNull Supplier<? extends Possible<? extends T>> supplier) {
        return this.isPresent() ? this : (Possible<T>) supplier.get();
    }

    /**
     * If this is absent, returns the {@code Possible} produced by the supplying function, otherwise
     * returns this {@code Possible}. A present one and an empty one are both returned as they are.
     *
     * @apiNote
     * This is the method the two states exist for. It lets a caller answer for absence without
     * disturbing an empty result, which {@link #or(Supplier)} cannot do and which {@link Optional}
     * cannot express at all.
     *
     * @param supplier a function that produces a fallback {@code Possible} for the absent state
     * @return the result of {@code supplier} if this is absent, otherwise this {@code Possible}
     */
    @SuppressWarnings("unchecked")
    public @NotNull Possible<T> orAbsent(@NotNull Supplier<? extends Possible<? extends T>> supplier) {
        return this.state == State.ABSENT ? (Possible<T>) supplier.get() : this;
    }

    // OrElse

    /**
     * If a value is present, returns the value, otherwise returns {@code other}.
     *
     * @param other the value to be returned if no value is present, may be {@code null}
     * @return the value if present, otherwise {@code other}
     */
    public @Nullable T orElse(@Nullable T other) {
        return this.value != null ? this.value : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result produced by the
     * supplying function.
     *
     * @param supplier the supplying function that produces a fallback value
     * @return the value if present, otherwise the result of {@code supplier}
     */
    public @Nullable T orElseGet(@NotNull Supplier<? extends T> supplier) {
        return this.value != null ? this.value : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws {@link NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Possible}
     * @throws NoSuchElementException if no value is present
     */
    public @NotNull T orElseThrow() {
        return this.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception produced by the
     * exception supplying function.
     *
     * @apiNote
     * A method reference to the exception constructor with an empty argument list can be used as the
     * supplier. For example, {@code IllegalStateException::new}.
     *
     * @param <X> the type of the exception to be thrown
     * @param exceptionSupplier the supplying function that produces an exception to be thrown
     * @return the value, if present
     * @throws X if no value is present
     */
    public <X extends Throwable> @NotNull T orElseThrow(@NotNull Supplier<? extends X> exceptionSupplier) throws X {
        if (this.value == null)
            throw exceptionSupplier.get();

        return this.value;
    }

    // Optional / Stream

    /**
     * Returns an {@link Optional} describing this one's value, or an empty {@link Optional} if no
     * value is present.
     *
     * @apiNote
     * Both value-less states collapse onto {@link Optional#empty()}, because that is all
     * {@link Optional} can carry. Converting is therefore lossy in one direction and should happen at
     * the edge of an API rather than inside one.
     *
     * @return an {@link Optional} describing the value, or empty if no value is present
     */
    public @NotNull Optional<T> toOptional() {
        return Optional.ofNullable(this.value);
    }

    /**
     * If a value is present, returns a {@link Stream} containing only that value, otherwise returns an
     * empty {@link Stream}.
     *
     * @return a {@code Stream} containing the value if present, otherwise empty
     */
    public @NotNull Stream<T> stream() {
        return this.value != null ? Stream.of(this.value) : Stream.empty();
    }

    // Object

    /**
     * Returns a string representation of this {@code Possible} suitable for debugging.
     *
     * @return a non-empty string representation of this instance
     */
    @Override
    public @NotNull String toString() {
        return switch (this.state) {
            case PRESENT -> String.format("Possible[%s]", this.value);
            case EMPTY -> "Possible.empty";
            case ABSENT -> "Possible.absent";
        };
    }

}
