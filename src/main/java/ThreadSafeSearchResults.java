
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * creates a thread safe version of the search results class
 * 
 * @author colininns
 *
 */
public class ThreadSafeSearchResults implements SearchResultsInterface {
	/**
	 * the work queue
	 */
	private final WorkQueue queue;

	/**
	 * the results of the search
	 */
	private final TreeMap<String, List<InvertedIndex.Result>> results;

	/**
	 * the inverted index the results are coming from
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * constructor for thread safe search results
	 * 
	 * @param myInvertedIndex the index we will get the results from
	 * @param queue           the work queue
	 */
	public ThreadSafeSearchResults(ThreadSafeInvertedIndex myInvertedIndex, WorkQueue queue) {
		results = new TreeMap<String, List<InvertedIndex.Result>>();
		this.index = myInvertedIndex;
		this.queue = queue;
	}

	@Override
	public Set<String> getResultKeySet() {
		synchronized (results) {
			return Collections.unmodifiableSet(results.keySet());
		}
	}

	@Override
	public int size(String query) {
		synchronized (results) {
			if (this.results.containsKey(query)) {
				return this.results.get(query).size();
			} else {
				return -1;
			}
		}
	}

	@Override
	public void write(Path output) throws IOException {
		synchronized (results) {
			SimpleJsonWriter.asSearchResult(results, output);
		}
	}

	@Override
	public void search(String queryLine, boolean exact) {
		queue.execute(new Task(queryLine, exact));
	}

	@Override
	public void search(Path queryPath, boolean exact) throws IOException {
		SearchResultsInterface.super.search(queryPath, exact);
		queue.finish();
	}

	/**
	 * Task class creates the tasks that will be paralleled
	 * 
	 * @author colininns
	 *
	 */
	private class Task implements Runnable {

		/** the text of the query */
		private final String line;

		/** which test to run */
		private final boolean exact;

		/**
		 * constructor for task
		 * 
		 * @param line  the string line we are testing
		 * @param exact tells us what type of search
		 */
		public Task(String line, boolean exact) {
			this.line = line;
			this.exact = exact;
		}

		@Override
		public void run() {
			TreeSet<String> parsed = TextFileStemmer.uniqueStems(line);
			if (!parsed.isEmpty()) {
				String joined = String.join(" ", parsed);
				synchronized (results) {
					if (results.containsKey(joined)) {
						return;
					}
				}
				var local = index.search(parsed, exact);
				synchronized (results) {
					results.put(joined, local);
				}
			}
		}
	}

}
