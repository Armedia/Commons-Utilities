package com.armedia.commons.utilities.function;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CheckedTools {

	public static <T, EX extends Throwable> CheckedConsumer<T, EX> check(Consumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return (T t) -> consumer.accept(t);
	}

	public static <T, R, EX extends Throwable> CheckedFunction<T, R, EX> check(Function<T, R> function) {
		Objects.requireNonNull(function);
		return (T t) -> function.apply(t);
	}

	public static <T, EX extends Throwable> CheckedPredicate<T, EX> check(Predicate<T> predicate) {
		Objects.requireNonNull(predicate);
		return (t) -> predicate.test(t);
	}

	public static <EX extends Throwable> CheckedRunnable<EX> check(Runnable runnable) {
		Objects.requireNonNull(runnable);
		return () -> runnable.run();
	}

	public static <T, EX extends Throwable> CheckedSupplier<T, EX> check(Supplier<T> supplier) {
		Objects.requireNonNull(supplier);
		return () -> supplier.get();
	}

	public static <T, U, EX extends Throwable> CheckedBiConsumer<T, U, EX> check(BiConsumer<T, U> consumer) {
		Objects.requireNonNull(consumer);
		return (T t, U u) -> consumer.accept(t, u);
	}

	public static <T, U, R, EX extends Throwable> CheckedBiFunction<T, U, R, EX> check(BiFunction<T, U, R> function) {
		Objects.requireNonNull(function);
		return (T t, U u) -> function.apply(t, u);
	}

	public static <T, U, EX extends Throwable> CheckedBiPredicate<T, U, EX> check(BiPredicate<T, U> predicate) {
		Objects.requireNonNull(predicate);
		return (T t, U u) -> predicate.test(t, u);
	}

	public static <T, U, V, EX extends Throwable> CheckedTriConsumer<T, U, V, EX> check(TriConsumer<T, U, V> consumer) {
		Objects.requireNonNull(consumer);
		return (T t, U u, V v) -> consumer.accept(t, u, v);
	}

	public static <T, U, V, R, EX extends Throwable> CheckedTriFunction<T, U, V, R, EX> check(
		TriFunction<T, U, V, R> function) {
		Objects.requireNonNull(function);
		return (T t, U u, V v) -> function.apply(t, u, v);
	}

	public static <T, U, V, EX extends Throwable> CheckedTriPredicate<T, U, V, EX> check(
		TriPredicate<T, U, V> predicate) {
		Objects.requireNonNull(predicate);
		return (T t, U u, V v) -> predicate.test(t, u, v);
	}

}