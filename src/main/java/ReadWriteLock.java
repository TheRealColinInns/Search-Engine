import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Maintains a pair of associated locks, one for read-only operations and one
 * for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive. The
 * active writer is able to acquire read or write locks as long as it is active.
 *
 * <!-- simplified lock used for this class -->
 * 
 * @see SimpleLock
 * 
 *      <!-- built-in Java locks that are similar (but more complex) -->
 * @see Lock
 * @see ReentrantLock
 * @see ReadWriteLock
 * @see ReentrantReadWriteLock
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2021
 */
public class ReadWriteLock {
	/** The conditional lock used for reading. */
	private final SimpleLock readerLock;

	/** The conditional lock used for writing. */
	private final SimpleLock writerLock;

	/** The number of active readers. */
	private int readers;

	/** The number of active writers; */
	private int writers;

	/** The thread that holds the write lock. */
	private Thread activeWriter;

	/**
	 * The lock object used for synchronized access of readers and writers. For
	 * security reasons, a separate private final lock object is used.
	 *
	 * @see <a href=
	 *      "https://wiki.sei.cmu.edu/confluence/display/java/LCK00-J.+Use+private+final+lock+objects+to+synchronize+classes+that+may+interact+with+untrusted+code">
	 *      SEI CERT Oracle Coding Standard for Java</a>
	 */
	private final Object lock;

	/**
	 * Initializes a new simple read/write lock.
	 */
	public ReadWriteLock() {
		readerLock = new SimpleReadLock();
		writerLock = new SimpleWriteLock();

		lock = new Object();

		readers = 0;
		writers = 0;

		activeWriter = null;
	}

	/**
	 * Returns the reader lock.
	 *
	 * @return the reader lock
	 */
	public SimpleLock readLock() {
		return readerLock;
	}

	/**
	 * Returns the writer lock.
	 *
	 * @return the writer lock
	 */
	public SimpleLock writeLock() {
		return writerLock;
	}

	/**
	 * Returns the number of active readers.
	 *
	 * @return the number of active readers
	 */
	public int readers() {
		synchronized (lock) {
			return readers;
		}
	}

	/**
	 * Returns the number of active writers.
	 *
	 * @return the number of active writers
	 */
	public int writers() {
		synchronized (lock) {
			return writers;
		}
	}

	/**
	 * Determines whether the thread running this code and the writer thread are in
	 * fact the same thread.
	 *
	 * @return true if the thread running this code and the writer thread are not
	 *         null and are the same thread
	 *
	 * @see Thread#currentThread()
	 */
	public boolean isActiveWriter() {
		synchronized (lock) {
			return Thread.currentThread().equals(activeWriter);
		}
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	private class SimpleReadLock implements SimpleLock {
		/**
		 * If the active thread already holds the write lock, allows it to continue.
		 * Otherwise, if there are active writers, then the thread is forced to wait
		 * until there are no active writers left. Once safe, allows the thread to
		 * acquire a read lock by incrementing the number of active readers.
		 */
		@Override
		public void lock() {
			synchronized (lock) {
				while (writers > 0 && !isActiveWriter()) {
					try {
						lock.wait();
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
				readers++;
			}
		}

		/**
		 * Will decrease the number of active readers, and notify any waiting threads if
		 * necessary.
		 *
		 * @throws IllegalStateException if no readers to unlock
		 */
		@Override
		public void unlock() throws IllegalStateException {
			synchronized (lock) {
				if (readers <= 0) {
					throw new IllegalStateException();
				}
				readers--;
				if (readers == 0) {
					lock.notifyAll();
				}

			}
		}
	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	private class SimpleWriteLock implements SimpleLock {
		/**
		 * If the active thread already holds the write lock, allows it to continue.
		 * Otherwise, if there are active readers or writers, then the thread is forced
		 * to wait until there are no active readers or writers left. Once safe, allows
		 * the thread to acquire a write lock by setting the active writer reference and
		 * incrementing the number of active writers.
		 */
		@Override
		public void lock() {
			synchronized (lock) {
				while ((readers > 0 || writers > 0) && !isActiveWriter()) {
					try {
						lock.wait();
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
				writers++;
				activeWriter = Thread.currentThread();

			}
		}

		/**
		 * Will decrease the number of active writers, and notify any waiting threads if
		 * necessary.
		 *
		 * @throws IllegalStateException           if no writers to unlock
		 * @throws ConcurrentModificationException if there are writers but unlock is
		 *                                         called by a thread that does not hold
		 *                                         the write lock
		 */
		@Override
		public void unlock() throws IllegalStateException, ConcurrentModificationException {
			synchronized (lock) {
				if (writers <= 0) {
					throw new IllegalStateException();
				}
				if (!isActiveWriter()) {
					throw new ConcurrentModificationException();
				}
				writers--;
				if (writers == 0) {
					activeWriter = null;
					lock.notifyAll();
				}

			}
		}
	}
}
