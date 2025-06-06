/*******************************************************************************
 * #%L
 * Armedia Caliente
 * %%
 * Copyright (C) 2013 - 2025 Armedia, LLC
 * %%
 * This file is part of the Caliente software.
 * 
 * If the software was purchased under a paid Caliente license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * 
 * Caliente is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caliente is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Caliente. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.armedia.commons.utilities.concurrent;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceableReentrantLock extends ReentrantLock implements Traceable {
	private static final long serialVersionUID = 1L;

	private static final AtomicLong LOCK_COUNTER = new AtomicLong(0);

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final AtomicLong conditionCounter = new AtomicLong(0);
	private final Serializable id;
	private final String name;

	public TraceableReentrantLock() {
		this(false);
	}

	public TraceableReentrantLock(Serializable id) {
		this(id, false);
	}

	public TraceableReentrantLock(boolean fair) {
		this(String.format("%016x", TraceableReentrantLock.LOCK_COUNTER.getAndIncrement()), fair);
	}

	public TraceableReentrantLock(Serializable id, boolean fair) {
		super(fair);
		this.id = Objects.requireNonNull(id, "Must provide a non-null ID");
		this.name = Traceable.format("ReentrantLock[{}]", this.id);
		this.log.trace("{}.constructed()", this.name);
	}

	@Override
	public Logger getLog() {
		return this.log;
	}

	@Override
	public Serializable getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public final Serializable getLockId() {
		return this.id;
	}

	@Override
	public void lock() {
		trace(super::lock, "lock");
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		trace(super::lockInterruptibly, "lockInterruptibly");
	}

	@Override
	public boolean tryLock() {
		return trace(() -> super.tryLock(), "tryLock");
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return trace(() -> super.tryLock(time, unit), "tryLock", time, unit);
	}

	@Override
	public void unlock() {
		trace(super::unlock, "unlock");
	}

	@Override
	public Condition newCondition() {
		final Condition condition = super.newCondition();
		final String conditionId = String.format("%016x", this.conditionCounter.getAndIncrement());
		return new TraceableCondition(this.log, this.name, conditionId, condition);
	}
}
