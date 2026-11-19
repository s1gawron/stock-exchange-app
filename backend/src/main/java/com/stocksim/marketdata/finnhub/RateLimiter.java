package com.stocksim.marketdata.finnhub;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** Simple token bucket: {@code permitsPerMinute} tokens, refilled continuously. Thread-safe. */
class RateLimiter {

	private final int capacity;
	private final double nanosPerPermit;
	private final ReentrantLock lock = new ReentrantLock();
	private double available;
	private long lastRefill;

	RateLimiter(int permitsPerMinute) {
		this.capacity = permitsPerMinute;
		this.nanosPerPermit = TimeUnit.MINUTES.toNanos(1) / (double) permitsPerMinute;
		this.available = permitsPerMinute;
		this.lastRefill = System.nanoTime();
	}

	/** Waits up to {@code maxWait} for a permit. Returns false when none became free in time. */
	boolean tryAcquire(Duration maxWait) {
		long deadline = System.nanoTime() + maxWait.toNanos();
		while (true) {
			long waitNanos;
			lock.lock();
			try {
				refill();
				if (available >= 1) {
					available -= 1;
					return true;
				}
				waitNanos = (long) ((1 - available) * nanosPerPermit);
			}
			finally {
				lock.unlock();
			}
			if (System.nanoTime() + waitNanos > deadline) {
				return false;
			}
			try {
				new Semaphore(0).tryAcquire(waitNanos, TimeUnit.NANOSECONDS);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return false;
			}
		}
	}

	private void refill() {
		long now = System.nanoTime();
		available = Math.min(capacity, available + (now - lastRefill) / nanosPerPermit);
		lastRefill = now;
	}
}
