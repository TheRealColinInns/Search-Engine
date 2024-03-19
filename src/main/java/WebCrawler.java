import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * class that efficiently crawls through the web using a multithreaded aproach
 * 
 * @author colininns
 *
 */
public class WebCrawler {
	/** stores all the urls we used */
	private final HashSet<String> usedUrls;
	/** the max urls */
	private final Integer max;

	/**
	 * constructor for web crawler
	 */
	public WebCrawler(int max) {
		usedUrls = new HashSet<String>();
		this.max = max;
	}

	/**
	 * crawls through from a desired seed
	 * 
	 * @param seed            the starting url
	 * @param inputmax        the max amount of urls
	 * @param queue           the work queue
	 * @param myInvertedIndex the index we add to
	 * @throws IOException in case we ahve a problem reading
	 */
	public void crawl(URL seed, WorkQueue queue, InvertedIndex myInvertedIndex) throws IOException {
		usedUrls.add(seed.toString());
		queue.execute(new Task(seed, myInvertedIndex, queue));
		queue.finish();
	}

	/**
	 * Creates a runnable task
	 * 
	 * @author colininns
	 *
	 */
	private class Task implements Runnable {
		/** the seed url */
		private URL seed;
		/** the index */
		private InvertedIndex myInvertedIndex;
		/** the work queue */
		private WorkQueue queue;



		/**
		 * Constructor for the task
		 * 
		 * @param seed            the seed url
		 * @param myInvertedIndex the index
		 * @param queue           the workqueue
		 * @param inputmax        the max urls
		 * @param usedUrls        the used urls
		 */
		public Task(URL seed, InvertedIndex myInvertedIndex, WorkQueue queue) {
			this.seed = seed;
			this.myInvertedIndex = myInvertedIndex;
			this.queue = queue;
		}

		@Override
		public void run() {
			ArrayList<URL> urlList = new ArrayList<URL>();
			String html = HtmlFetcher.fetch(seed, 3);
			if (html != null) {
				html = HtmlCleaner.stripBlockElements(html);
				urlList = LinkParser.getValidLinks(seed, html);
				synchronized (usedUrls) {
					for (URL currentUrl : urlList) {
						if (usedUrls.size() < max) {
							if (usedUrls.add(currentUrl.toString())) {
								queue.execute(new Task(currentUrl, myInvertedIndex, queue));
							} 
						} 
					}

				}
				myInvertedIndex.addAll(Arrays.asList(TextParser.parse(HtmlCleaner.stripHtml(html))), seed.toString());
			}
		}

	}
}