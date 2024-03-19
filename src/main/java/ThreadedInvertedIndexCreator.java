
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * creates an inverted index using multithreading
 * 
 * @author colininns
 *
 */
public class ThreadedInvertedIndexCreator extends InvertedIndexCreator {
	/**
	 * the method that calls all the other methods
	 *
	 * @param myInvertedIndex the array list that will be converted into the more
	 *                        useful data structure
	 * @param inputPath       the path that the arraylist is going to grab the data
	 *                        from
	 * @param workqueue       the word queue that will be used
	 * @throws IOException in case of io exception
	 */
	public static void createInvertedIndex(Path inputPath, ThreadSafeInvertedIndex myInvertedIndex, WorkQueue workqueue)
			throws IOException {
		if (Files.isDirectory(inputPath)) {
			directoryStemmer(inputPath, myInvertedIndex, workqueue);
		} else {
			singleFileStemmer(inputPath, myInvertedIndex, workqueue);
		}
		workqueue.finish();
	}

	/**
	 * stems a single file
	 *
	 * @param inputPath       the path that the arraylist is going to grab the data
	 *                        from
	 * @param myInvertedIndex the data structure we are building
	 * @param workqueue       the word queue that will be used
	 * @throws IOException in case unable to parse
	 */
	public static void singleFileStemmer(Path inputPath, ThreadSafeInvertedIndex myInvertedIndex, WorkQueue workqueue)
			throws IOException {
		String location = inputPath.toString();
		workqueue.execute(new Task(inputPath, myInvertedIndex, location));
	}

	/**
	 * stems a directory
	 *
	 * @param myInvertedIndex the data structure
	 * @param inputPath       the path that the arraylist is going to grab the data
	 *                        from
	 * @param workqueue       the word queue that will be used
	 * @throws IOException it really shouldn't throw tho
	 */
	private static void directoryStemmer(Path inputPath, ThreadSafeInvertedIndex myInvertedIndex, WorkQueue workqueue)
			throws IOException {
		for (Path currentPath : DirectoryNavigator.findPaths(inputPath)) {
			singleFileStemmer(currentPath, myInvertedIndex, workqueue);
		}

	}

	/**
	 * Task class creates the tasks that will be paralleled
	 * 
	 * @author colininns
	 *
	 */
	private static class Task implements Runnable {

		/** buffered reader */
		private final Path inputPath;

		/** the index we write to */
		private final ThreadSafeInvertedIndex myInvertedIndex;

		/**
		 * constructor for task
		 * 
		 * @param inputPath       the location we are getting it from
		 * @param myInvertedIndex the index we are building to
		 * @param location        the location we found it
		 */
		public Task(Path inputPath, ThreadSafeInvertedIndex myInvertedIndex, String location) {
			this.inputPath = inputPath;
			this.myInvertedIndex = myInvertedIndex;
		}

		@Override
		public void run() {
			InvertedIndex local = new InvertedIndex();
			try {
				InvertedIndexCreator.singleFileStemmer(inputPath, local);
				myInvertedIndex.addAll(local);
			} catch (IOException e) {
				System.out.println("Unable to rad path: " + inputPath);
			}
		}
	}

}
