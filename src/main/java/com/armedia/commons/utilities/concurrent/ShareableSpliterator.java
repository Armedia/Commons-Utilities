package com.armedia.commons.utilities.concurrent;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;

public class ShareableSpliterator<E> extends BaseShareableLockable implements Spliterator<E> {

	private final Spliterator<E> spliterator;

	public ShareableSpliterator(Spliterator<E> spliterator) {
		this(ShareableLockable.extractShareableLock(spliterator), spliterator);
	}

	public ShareableSpliterator(ReadWriteLock rwLock, Spliterator<E> spliterator) {
		super(rwLock);
		this.spliterator = Objects.requireNonNull(spliterator, "Must provide a non-null backing spliterator");
	}

	public ShareableSpliterator(ShareableLockable lockable, Spliterator<E> spliterator) {
		super(lockable);
		this.spliterator = Objects.requireNonNull(spliterator, "Must provide a non-null backing spliterator");
	}

	@Override
	public boolean tryAdvance(Consumer<? super E> action) {
		Objects.requireNonNull(action, "Must provide an action to apply on advancement");
		return shareLocked(() -> this.spliterator.tryAdvance(action));
	}

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		Objects.requireNonNull(action, "Must provide an action to apply on iteration");
		shareLocked(() -> this.spliterator.forEachRemaining(action));
	}

	@Override
	public Spliterator<E> trySplit() {
		return shareLocked(() -> {
			Spliterator<E> it = this.spliterator.trySplit();
			return (it != null ? new ShareableSpliterator<>(this, it) : null);
		});
	}

	@Override
	public long estimateSize() {
		return shareLocked(this.spliterator::estimateSize);
	}

	@Override
	public long getExactSizeIfKnown() {
		return shareLocked(this.spliterator::getExactSizeIfKnown);
	}

	@Override
	public int characteristics() {
		return this.spliterator.characteristics();
	}

	@Override
	public boolean hasCharacteristics(int characteristics) {
		return this.spliterator.hasCharacteristics(characteristics);
	}

	@Override
	public Comparator<? super E> getComparator() {
		return this.spliterator.getComparator();
	}
}