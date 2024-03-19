import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * creates a thread safe inverted index
 * 
 * @author colininns
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/** The indexLock used to protect concurrent access to the underlying set. */
	private final ReadWriteLock indexLock;

	/**
	 * constructor for thread safe index
	 */
	public ThreadSafeInvertedIndex() {
		super();
		indexLock = new ReadWriteLock();
	}

	/*
	 * Methods for index
	 */

	@Override
	public Collection<String> getWords() {
		indexLock.readLock().lock();
		try {
			return super.getWords();
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public Collection<String> getLocations(String key) {
		indexLock.readLock().lock();
		try {
			return super.getLocations(key);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public Collection<Integer> getPositions(String outerKey, String innerKey) {
		indexLock.readLock().lock();
		try {
			return super.getPositions(outerKey, innerKey);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public boolean containsWord(String key) {
		indexLock.readLock().lock();
		try {
			return super.containsWord(key);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public boolean containsLocation(String outerKey, String innerKey) {
		indexLock.readLock().lock();
		try {
			return super.containsLocation(outerKey, innerKey);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public boolean containsPosition(String outerKey, String innerKey, Integer value) {
		indexLock.readLock().lock();
		try {
			return super.containsPosition(outerKey, innerKey, value);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public int sizeWords() {
		indexLock.readLock().lock();
		try {
			return super.sizeWords();
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public int sizeLocations(String key) {
		indexLock.readLock().lock();
		try {
			return super.sizeLocations(key);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public int sizePositions(String outerKey, String innerKey) {
		indexLock.readLock().lock();
		try {
			return super.sizePositions(outerKey, innerKey);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		indexLock.readLock().lock();
		try {
			return super.toString();
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public void indexWriter(Path filename) throws IOException {
		indexLock.readLock().lock();
		try {
			super.indexWriter(filename);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public List<Result> exactSearch(Set<String> queries) {
		indexLock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public List<Result> partialSearch(Set<String> queries) {
		indexLock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public void add(String outerKey, String innerKey, Integer value) {
		indexLock.writeLock().lock();
		try {
			super.add(outerKey, innerKey, value);
		} finally {
			indexLock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(List<String> words, String location) {
		indexLock.writeLock().lock();
		try {
			super.addAll(words, location);
		} finally {
			indexLock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		indexLock.writeLock().lock();
		try {
			super.addAll(other);
		} finally {
			indexLock.writeLock().unlock();
		}
	}

	/*
	 * Methods for word count
	 */

	@Override
	public boolean containsWordCount(String location) {
		indexLock.readLock().lock();
		try {
			return super.containsWordCount(location);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public Integer getWordCount(String location) {
		indexLock.readLock().lock();
		try {
			return super.getWordCount(location);
		} finally {
			indexLock.readLock().unlock();
		}
	}

	@Override
	public void writeWordCount(Path countPath) throws IOException {
		indexLock.readLock().lock();
		try {
			super.writeWordCount(countPath);
		} finally {
			indexLock.readLock().unlock();
		}
	}

}
