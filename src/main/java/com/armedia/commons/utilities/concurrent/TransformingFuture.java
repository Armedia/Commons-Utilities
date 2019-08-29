package com.armedia.commons.utilities.concurrent;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.armedia.commons.utilities.function.CheckedFunction;
import com.armedia.commons.utilities.function.CheckedTools;

public class TransformingFuture<F, T> implements Future<T> {

	private final Future<F> future;
	private final CheckedFunction<F, T, ? extends Throwable> transformer;

	public TransformingFuture(Future<F> future, Function<F, T> transformer) {
		this(future, CheckedTools.check(transformer));
	}

	public <E extends Exception> TransformingFuture(Future<F> future,
		CheckedFunction<F, T, ? extends Throwable> transformer) {
		this.future = Objects.requireNonNull(future, "Must provide a future to transform with");
		this.transformer = Objects.requireNonNull(transformer, "Must provide a transformer function");
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return this.future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.future.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		final F result = this.future.get();
		try {
			return this.transformer.applyChecked(result);
		} catch (Throwable e) {
			throw new ExecutionException(e);
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		final F result = this.future.get(timeout, unit);
		try {
			return this.transformer.applyChecked(result);
		} catch (Throwable e) {
			throw new ExecutionException(e);
		}
	}
}