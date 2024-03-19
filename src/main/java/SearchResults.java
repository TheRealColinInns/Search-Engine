
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible for converting an array list into the map data structure
 * desired that will be very important later for creating the json file
 *
 * @author CS 212 Software Development - Colin Inns
 * @author University of San Francisco
 * @version Spring 2021
 */
public class SearchResults implements SearchResultsInterface {

	/**
	 * the results of the search
	 */
	private final TreeMap<String, List<InvertedIndex.Result>> results;

	/**
	 * the inverted index the results are coming from
	 */
	private final InvertedIndex index;

	/**
	 * the constructor for this class
	 * 
	 * @param myInvertedIndex the index to get the results from
	 */
	public SearchResults(InvertedIndex myInvertedIndex) {
		results = new TreeMap<String, List<InvertedIndex.Result>>();
		index = myInvertedIndex;
	}

	@Override
	public void search(String queryLine, boolean exact) {
		TreeSet<String> parsed = TextFileStemmer.uniqueStems(queryLine);
		if (!parsed.isEmpty()) {
			String joined = String.join(" ", parsed);
			if (!results.containsKey(joined)) {
				results.put(joined, index.search(parsed, exact));
			}
		}
	}

	@Override
	public Set<String> getResultKeySet() {
		return Collections.unmodifiableSet(results.keySet());
	}

	@Override
	public int size(String query) {
		if (this.results.containsKey(query)) {
			return this.results.get(query).size();
		} else {
			return -1;
		}
	}

	@Override
	public void write(Path output) throws IOException {
		SimpleJsonWriter.asSearchResult(results, output);
	}
}
