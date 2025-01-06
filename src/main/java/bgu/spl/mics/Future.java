package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	
	volatile T result;
	boolean isResolved;
	
	public Future() {
		result = null;
		isResolved=false;
	}

	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     *
     */
	public T get() {
		return this.get(0,TimeUnit.MILLISECONDS);
	}

	/**
     * Resolves the result of this Future object.
     */
	public synchronized void resolve (T result) {//when i change an output
		this.result = result;
		this.isResolved = true;
		notifyAll();
	}

	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		return isResolved;
	}

	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not,
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public synchronized T get(long timeout, TimeUnit unit) {
		if (isResolved) return result;
		Long newtimeout = unit.toMillis(timeout); //new time
		while (!isDone()) {
			try {
				this.wait(newtimeout);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Interrupted");
			}
		}
		return result;
	}
}



