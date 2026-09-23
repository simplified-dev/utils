package dev.simplified.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Coverage of what {@link Possible} promises: that every method it shares with {@link Optional}
 * answers as {@link Optional} would, in all three states, and that the absent state survives the
 * operations a caller chains onto it.
 */
@DisplayName("Possible is Optional with an absent state")
class PossibleTest {

    /**
     * One {@code Possible} in each of the three states.
     */
    private static final List<Possible<String>> EVERY_STATE = List.of(Possible.of("x"), Possible.empty(), Possible.absent());

    /**
     * Method names every class inherits, which say nothing about parity with {@link Optional}.
     */
    private static final Set<String> OBJECT_METHODS = Set.of("equals", "hashCode", "toString");

    @Test
    @DisplayName("every public method Optional declares exists on Possible with the same parameters, return shape and staticness")
    void everyOptionalMethodHasATwin() throws NoSuchMethodException {
        for (Method method : Optional.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || OBJECT_METHODS.contains(method.getName()))
                continue;

            Method twin = Possible.class.getMethod(method.getName(), method.getParameterTypes());
            Class<?> expectedReturn = method.getReturnType() == Optional.class ? Possible.class : method.getReturnType();
            assertThat(method + " returns " + twin.getReturnType().getName(), twin.getReturnType() == expectedReturn, is(true));
            assertThat(method + " staticness", Modifier.isStatic(twin.getModifiers()), is(Modifier.isStatic(method.getModifiers())));
        }
    }

    @Test
    @DisplayName("no public method on Possible reuses a name of Optional's with a signature Optional does not declare")
    void noSharedNameIsReshaped() {
        Set<String> optionalNames = Arrays.stream(Optional.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .filter(name -> !OBJECT_METHODS.contains(name))
            .collect(Collectors.toSet());

        for (Method method : Possible.class.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || !optionalNames.contains(method.getName()))
                continue;

            boolean declaredByOptional = Arrays.stream(Optional.class.getMethods())
                .anyMatch(twin -> twin.getName().equals(method.getName()) && Arrays.equals(twin.getParameterTypes(), method.getParameterTypes()));
            assertThat(method.toString(), declaredByOptional, is(true));
        }
    }

    @Test
    @DisplayName("every method Possible shares with Optional answers as Optional does, in all three states")
    void everySharedMethodAnswersAsOptionalDoes() {
        for (Possible<String> possible : EVERY_STATE) {
            Optional<String> optional = possible.toOptional();
            String state = possible.toString();

            assertThat(state + " get", outcome(possible::get), is(outcome(optional::get)));
            assertThat(state + " isPresent", possible.isPresent(), is(optional.isPresent()));
            assertThat(state + " isEmpty", possible.isEmpty(), is(optional.isEmpty()));
            assertThat(state + " ifPresent", effects(sink -> possible.ifPresent(sink::add)), is(effects(sink -> optional.ifPresent(sink::add))));
            assertThat(state + " ifPresentOrElse", effects(sink -> possible.ifPresentOrElse(sink::add, () -> sink.add("else"))), is(effects(sink -> optional.ifPresentOrElse(sink::add, () -> sink.add("else")))));
            assertThat(state + " filter kept", outcome(() -> possible.filter(value -> true)), is(outcome(() -> optional.filter(value -> true))));
            assertThat(state + " filter rejected", outcome(() -> possible.filter(value -> false)), is(outcome(() -> optional.filter(value -> false))));
            assertThat(state + " map", outcome(() -> possible.map(String::length)), is(outcome(() -> optional.map(String::length))));
            assertThat(state + " map to null", outcome(() -> possible.map(value -> null)), is(outcome(() -> optional.map(value -> null))));
            assertThat(state + " flatMap to a value", outcome(() -> possible.flatMap(value -> Possible.of(value + "!"))), is(outcome(() -> optional.flatMap(value -> Optional.of(value + "!")))));
            assertThat(state + " flatMap to nothing", outcome(() -> possible.flatMap(value -> Possible.absent())), is(outcome(() -> optional.flatMap(value -> Optional.empty()))));
            assertThat(state + " or", outcome(() -> possible.or(() -> Possible.of("y"))), is(outcome(() -> optional.or(() -> Optional.of("y")))));
            assertThat(state + " stream", possible.stream().toList(), is(optional.stream().toList()));
            assertThat(state + " orElse", possible.orElse("y"), is(optional.orElse("y")));
            assertThat(state + " orElseGet", possible.orElseGet(() -> "y"), is(optional.orElseGet(() -> "y")));
            assertThat(state + " orElseThrow", outcome(possible::orElseThrow), is(outcome(optional::orElseThrow)));
            assertThat(state + " orElseThrow(Supplier)", outcome(() -> possible.orElseThrow(IllegalStateException::new)), is(outcome(() -> optional.orElseThrow(IllegalStateException::new))));
        }
    }

    @Test
    @DisplayName("empty and absent are not equal, and hash apart")
    void theTwoStatesAreDistinct() {
        assertThat(Possible.empty(), is(not(Possible.absent())));
        assertThat(Possible.absent(), is(not(Possible.empty())));
        assertThat(Possible.empty().hashCode() == Possible.absent().hashCode(), is(false));
    }

    @Test
    @DisplayName("each state is equal to itself")
    void eachStateIsReflexive() {
        assertThat(Possible.empty(), is(Possible.empty()));
        assertThat(Possible.absent(), is(Possible.absent()));
        assertThat(Possible.of("x"), is(Possible.of("x")));
        assertThat(Possible.of("x"), is(not(Possible.of("y"))));
    }

    @Test
    @DisplayName("isEmpty covers both value-less states, isAbsent only the stronger one, and getState names each")
    void thePredicatesNest() {
        assertThat(Possible.of("x").getState(), is(Possible.State.PRESENT));
        assertThat(Possible.empty().getState(), is(Possible.State.EMPTY));
        assertThat(Possible.absent().getState(), is(Possible.State.ABSENT));
        assertThat(Possible.empty().isEmpty(), is(true));
        assertThat(Possible.absent().isEmpty(), is(true));
        assertThat(Possible.of("x").isEmpty(), is(false));
        assertThat(Possible.absent().isAbsent(), is(true));
        assertThat(Possible.empty().isAbsent(), is(false));
        assertThat(Possible.of("x").isAbsent(), is(false));
    }

    @Test
    @DisplayName("a value-less state survives map, flatMap and filter")
    void theStateSurvivesTransformation() {
        assertThat(Possible.absent().map(Object::toString).getState(), is(Possible.State.ABSENT));
        assertThat(Possible.empty().map(Object::toString).getState(), is(Possible.State.EMPTY));
        assertThat(Possible.absent().flatMap(Possible::of).getState(), is(Possible.State.ABSENT));
        assertThat(Possible.empty().flatMap(Possible::of).getState(), is(Possible.State.EMPTY));
        assertThat(Possible.absent().filter(value -> true).getState(), is(Possible.State.ABSENT));
    }

    @Test
    @DisplayName("map takes the one type argument Optional's does, so code written against Optional compiles")
    void mapMatchesOptionalsSignature() {
        Possible<Number> widened = Possible.of("x").<Number>map(String::length);
        assertThat(widened.get().intValue(), is(1));
    }

    @Test
    @DisplayName("orAbsent answers for absence alone and leaves an empty one empty")
    void orAbsentIsSelective() {
        assertThat(Possible.absent().orAbsent(() -> Possible.of("sub")).orElse(null), is("sub"));
        assertThat(Possible.empty().orAbsent(() -> Possible.of("sub")).getState(), is(Possible.State.EMPTY));
        assertThat(Possible.of("kept").orAbsent(() -> Possible.of("sub")).orElse(null), is("kept"));
    }

    @Test
    @DisplayName("a rejected filter yields empty rather than absent, because the container held a value to test")
    void filterYieldsEmpty() {
        assertThat(Possible.of("x").filter(value -> false).getState(), is(Possible.State.EMPTY));
    }

    @Test
    @DisplayName("an Optional converts to present or empty, and back without loss")
    void optionalRoundTrips() {
        assertThat(Possible.ofOptional(Optional.empty()).getState(), is(Possible.State.EMPTY));
        assertThat(Possible.ofOptional(Optional.of("x")).getState(), is(Possible.State.PRESENT));
        assertThat(Possible.ofOptional(Optional.of("x")).toOptional(), is(Optional.of("x")));
        assertThat(Possible.ofOptional(Optional.empty()).toOptional(), is(Optional.empty()));
        assertThat(Possible.absent().toOptional(), is(Optional.empty()));
    }

    @Test
    @DisplayName("get on a value-less Possible names which of the two states it was")
    void getNamesTheState() {
        assertThat(assertThrows(NoSuchElementException.class, () -> Possible.empty().get()).getMessage(), is("No value present"));
        assertThat(assertThrows(NoSuchElementException.class, () -> Possible.absent().get()).getMessage(), is("Value absent"));
    }

    /**
     * Runs a call and reduces its result to something comparable across the two types: a
     * {@code Possible} as the {@link Optional} it converts to, and a thrown exception as its class.
     *
     * @param call the call to run
     * @return the comparable outcome
     */
    private static Object outcome(Supplier<?> call) {
        try {
            Object result = call.get();
            return result instanceof Possible<?> possible ? possible.toOptional() : result;
        } catch (RuntimeException exception) {
            return exception.getClass();
        }
    }

    /**
     * Collects what a void call did, so two of them can be compared.
     *
     * @param body the call, given a sink to record into
     * @return everything the call recorded, in order
     */
    private static List<String> effects(Consumer<List<String>> body) {
        List<String> sink = new ArrayList<>();
        body.accept(sink);
        return sink;
    }

}
