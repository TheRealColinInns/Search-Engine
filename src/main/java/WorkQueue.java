import java.util.LinkedList;

/**
 * A simple work queue implementation based on the IBM developerWorks article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @see <a href="https://www.ibm.com/developerworks/library/j-jtp0730/"> Java
 *      Theory and Practice: Thread Pools and Work Queues</a>
 * 
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2021
 */
public class WorkQueue {

	/**
	 * Pool of worker threads that will wait in the background until work is
	 * available.
	 */
	private final Worker[] workers;

	/** Queue of pending work requests. */
	private final LinkedList<Runnable> queue;

	/** Used to signal the queue should be shutdown. */
	private volatile boolean shutdown;

	/** The default number of threads to use when not specified. */
	public static final int DEFAULT = 5;

	/** Logger used for this class. */
	// private static final Logger log = LogManager.getLogger();

	/** pending tracks how many pending work. */
	private int pending;

	/**
	 * Starts a work queue with the default number of threads.
	 *
	 * @see #WorkQueue(int)
	 */
	public WorkQueue() {
		this(DEFAULT);
	}

	/**
	 * Starts a work queue with the specified number of threads.
	 *
	 * @param threads number of worker threads; should be greater than 1
	 */
	public WorkQueue(int threads) {
		this.queue = new LinkedList<Runnable>();
		this.workers = new Worker[threads];
		this.pending = 0;

		shutdown = false;

		// start the threads so they are waiting in the background
		for (int i = 0; i < threads; i++) {
			workers[i] = new Worker();
			workers[i].start();
		}

	}

	/**
	 * Adds a work request to the queue. A thread will process this request when
	 * available.
	 *
	 * @param task work request (in the form of a {@link Runnable} object)
	 */
	public void execute(Runnable task) {
		synchronized (this) {
			pending++;
		}
		synchronized (queue) {
			queue.addLast(task);
			queue.notifyAll();
		}
	}

	/**
	 * Waits for all pending work to be finished. Does not terminate the worker
	 * threads so that the work queue can continue to be used.
	 */
	public synchronized void finish() {
		while (pending > 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				System.err.println("Warning: Work queue interrupted while finishing.");
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished, but
	 * threads in-progress will not be interrupted.
	 */
	public void shutdown() {
		// safe to do unsynchronized due to volatile keyword
		shutdown = true;

		synchronized (queue) {
			queue.notifyAll();
		}
	}

	/**
	 * Similar to {@link Thread#join()}, waits for all the work to be finished and
	 * the worker threads to terminate. The work queue cannot be reused after this
	 * call completes.
	 */
	public void join() {
		finish();
		shutdown();

		for (Worker worker : workers) {
			try {
				worker.join();
			} catch (InterruptedException e) {
				System.err.println("Warning: Work queue interrupted while joining.");

				Thread.currentThread().interrupt();
			}
		}

	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads
	 */
	public int size() {
		return workers.length;
	}

	/**
	 * decrements the pending
	 */
	private synchronized void decrementPending() {
		if (pending > 0) {
			pending--;
		}
		if (pending == 0) {
			this.notifyAll();
		}
	}

	/**
	 * Waits until work is available in the work queue. When work is found, will
	 * remove the work from the queue and run it. If a shutdown is detected, will
	 * exit instead of grabbing new work from the queue. These threads will continue
	 * running in the background until a shutdown is requested.
	 */
	private class Worker extends Thread {
		/**
		 * Initializes a worker thread with a custom name.
		 */
		public Worker() {
			setName("Worker" + getName());
		}

		@Override
		public void run() {
			Runnable task = null;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty() && !shutdown) {
						try {

							queue.wait();
						} catch (InterruptedException e) {
							System.err.println("Warning: Work queue interrupted while waiting.");

							Thread.currentThread().interrupt();
						}
					}

					if (shutdown) {

						break;
					} else {
						task = queue.removeFirst();
					}
				}

				try {

					task.run();

				} catch (RuntimeException e) {

					System.err.println("Warning: Work queue encountered an exception while running.");

				} finally {
					decrementPending();
				}
			}
		}
	}

}