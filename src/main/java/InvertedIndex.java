
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for storing the data structure See the README for details.
 *
 * @author CS 212 Software Development - Colin Inns
 * @author University of San Francisco
 * @version Spring 2021
 */
public class InvertedIndex {

	/**
	 * this is our data structure
	 */
	private final TreeMap<String, TreeMap<String, Collection<Integer>>> index;

	/**
	 * this is our wordCount map
	 */
	private final TreeMap<String, Integer> wordCount;
	
	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Constructor for inverted index
	 */
	public InvertedIndex() {
		index = new TreeMap<String, TreeMap<String, Collection<Integer>>>();
		wordCount = new TreeMap<String, Integer>();
	}

	/*
	 * +--------------------------------------------------------------------------+
	 * Methods for index:
	 */

	/**
	 * Getter for the inverted index
	 * 
	 * @return my inverted index for a specific instance
	 * 
	 */
	public Collection<String> getWords() {
		return Collections.unmodifiableCollection(this.index.keySet());
	}

	/**
	 * Getter for the nested map inside the inverted index
	 * 
	 * @param key word
	 * @return the nested map inside the inverted index for a specified key, null if
	 *         it doesn't exist
	 * 
	 */
	public Collection<String> getLocations(String key) {
		if (this.containsWord(key)) {
			return Collections.unmodifiableCollection(this.index.get(key).keySet());
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Getter for the nested array inside the inverted index
	 * 
	 * @param outerKey word
	 * @param innerKey location
	 * @return the nested array inside the inverted index
	 * @throws NullPointerException if inner or outer key dont exist
	 */
	public Collection<Integer> getPositions(String outerKey, String innerKey) {
		if (this.containsLocation(outerKey, innerKey)) {
			return Collections.unmodifiableCollection(this.index.get(outerKey).get(innerKey));
		}
		return Collections.emptySet();
	}

	/**
	 * Contains method for the entire inverted index
	 * 
	 * @param key word
	 * @return {@code true} if the inverted index has a specified key
	 * 
	 */
	public boolean containsWord(String key) {
		return this.index.containsKey(key);
	}

	/**
	 * Contains method for the nested map
	 * 
	 * @param outerKey word
	 * @param innerKey location
	 * @return {@code true} if the inverted index has a specified key
	 * 
	 */
	public boolean containsLocation(String outerKey, String innerKey) {
		if (this.containsWord(outerKey)) {
			return this.index.get(outerKey).containsKey(innerKey);
		} else {
			return false;
		}
	}

	/**
	 * contains method for the nested array
	 * 
	 * @param outerKey word
	 * @param innerKey location
	 * @param value    position
	 * @return {@code true} if the inverted index has a specified key
	 * 
	 */
	public boolean containsPosition(String outerKey, String innerKey, Integer value) {
		if (this.containsLocation(outerKey, innerKey)) {
			return this.index.get(outerKey).get(innerKey).contains(value);
		} else {
			return false;
		}
	}

	/**
	 * add method for the nested array
	 * 
	 * @param word     word
	 * @param location location
	 * @param value    position
	 */
	public void add(String word, String location, Integer value) {
		Stemmer stemmer = new SnowballStemmer(DEFAULT);
		word = stemmer.stem(word).toString();
		this.index.putIfAbsent(word, new TreeMap<>());
		this.index.get(word).putIfAbsent(location, new TreeSet<>());
		boolean modified = this.index.get(word).get(location).add(value);
		if (modified) {
			this.addToWordCount(location);
		}
	}

	/**
	 * size method for the entire inverted index
	 * 
	 * @return int the size of the map
	 */
	public int sizeWords() {
		return this.index.size();
	}

	/**
	 * size method for the entire inverted index
	 * 
	 * @param key word
	 * @return int the size of the map
	 */
	public int sizeLocations(String key) {
		if (this.containsWord(key)) {
			return this.index.get(key).size();
		} else {
			return -1;
		}
	}

	/**
	 * size method for the entire inverted index
	 * 
	 * @param outerKey word
	 * @param innerKey location
	 * @return int the size of the arraylist
	 */
	public int sizePositions(String outerKey, String innerKey) {
		if (this.containsLocation(outerKey, innerKey)) {
			return this.index.get(outerKey).get(innerKey).size();
		} else {
			return -1;
		}
	}

	@Override
	public String toString() {
		return index.toString();
	}

	/**
	 * writes our inverted index into a specified file
	 *
	 * @param filename this is the file name that we are going to write the inverted
	 *                 index to
	 * @throws IOException Catch this is driver
	 */
	public void indexWriter(Path filename) throws IOException {
		SimpleJsonWriter.asNestedArray(this.index, filename);
	}

	/**
	 * adds an entire list into the index
	 * 
	 * @param words    the words to input
	 * @param location the location the words were found
	 */
	public void addAll(List<String> words, String location) {
		int position = 0;
		for (String word : words) {
			position++;
			this.add(word, location, position);
		}
	}

	/**
	 * adds an entire index to the index
	 * 
	 * @param other the index to add
	 */
	public void addAll(InvertedIndex other) {
		// first part adds the words
		for (String word : other.index.keySet()) {
			if (this.index.containsKey(word)) {
				for (String location : other.index.get(word).keySet()) {
					if (this.index.get(word).containsKey(location)) {
						this.index.get(word).get(location).addAll(other.index.get(word).get(location));
					} else {
						this.index.get(word).put(location, other.index.get(word).get(location));
					}
				}
			} else {
				this.index.put(word, other.index.get(word));
			}
		}
		// second part updates the word counts
		for (String location : other.wordCount.keySet()) {
			if (this.wordCount.containsKey(location)) {
				this.wordCount.put(location, this.wordCount.get(location) + other.wordCount.get(location));
			} else {
				this.wordCount.put(location, other.wordCount.get(location));
			}
		}

	}

	/**
	 * searches the index for exact queries
	 * 
	 * @param queries the queries we are searching for
	 * @return a list of results, in order
	 */
	public List<Result> exactSearch(Set<String> queries) {
		Map<String, Result> lookup = new HashMap<String, Result>();
		List<Result> results = new ArrayList<>();

		for (String query : queries) {
			if (this.index.containsKey(query)) {
				this.lookup(lookup, results, query);
			}
		}

		Collections.sort(results);
		return results;
	}

	/**
	 * searches the index for parts of queries
	 * 
	 * @param queries the queries we are searching for
	 * @return a list of results, in order
	 */
	public List<Result> partialSearch(Set<String> queries) {
		Map<String, Result> lookup = new HashMap<>();
		List<Result> results = new ArrayList<>();

		for (String query : queries) {
			for (String word : this.index.tailMap(query).keySet()) {
				if (word.startsWith(query)) {
					this.lookup(lookup, results, word);
				} else {
					break;
				}
			}
		}

		Collections.sort(results);
		return results;
	}

	/**
	 * Helper function loops through the lookup to get the search results
	 * 
	 * @param lookup  the lookup map
	 * @param results the singular result
	 * @param word    the word we found
	 */
	private void lookup(Map<String, Result> lookup, List<Result> results, String word) {
		for (String path : this.index.get(word).keySet()) {
			if (!lookup.containsKey(path)) {
				Result local = new Result(path);
				lookup.putIfAbsent(path, local);
				results.add(local);
			}

			lookup.get(path).update(word);
		}
	}

	/**
	 * deterines whether to exact search or not
	 * 
	 * @param queries the query set
	 * @param exact   boolean if exact search
	 * @return a list of results
	 */
	public List<Result> search(Set<String> queries, boolean exact) {
		if (exact) {
			return this.exactSearch(queries);
		} else {
			return this.partialSearch(queries);
		}
	}

	/**
	 * contains method for word count
	 * 
	 * @param location the file
	 * @return if the file exists in the word count
	 */
	public boolean containsWordCount(String location) {
		return this.wordCount.containsKey(location);
	}

	/**
	 * add method for word count
	 * 
	 * @param location the file the count came from
	 */
	private void addToWordCount(String location) {
		this.wordCount.put(location, wordCount.getOrDefault(location, 0) + 1);
	}

	/**
	 * get method for word count
	 * 
	 * @param location the file we want to know the word count of
	 * @return the word count at that location
	 */
	public Integer getWordCount(String location) {
		return wordCount.getOrDefault(location, 0);
	}

	/**
	 * writes the word counts to a specified file
	 * 
	 * @param countPath the file we are writing to
	 * @throws IOException throws if file doesn't exist
	 */
	public void writeWordCount(Path countPath) throws IOException {
		SimpleJsonWriter.asObject(wordCount, countPath);
	}

	/*
	 * +--------------------------------------------------------------------------+
	 * These are methods for the query:
	 */

	/**
	 * Inner class that stores a single result
	 * 
	 * @author colininns
	 *
	 */
	public class Result implements Comparable<Result> {
		/**
		 * stores where the count came from
		 */
		private final String location;
		/**
		 * the amount of hits it found
		 */
		private int count;
		/**
		 * the hits divided by the word count
		 */
		private Double score;

		/**
		 * Constructor for the result
		 * 
		 * @param location the location
		 */
		public Result(String location) {
			this.location = location;
			this.count = 0;
			this.score = 0.0;
		}

		/**
		 * gets the location
		 * 
		 * @return a string of the location
		 */
		public String getLocation() {
			return location;
		}

		/**
		 * gets the count
		 * 
		 * @return an int of the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * gets the score
		 * 
		 * @return a double of the score
		 */
		public Double getScore() {
			return score;
		}

		/**
		 * updates the result with a new count and score
		 * 
		 * @param query the query
		 */
		private void update(String query) {
			this.count += index.get(query).get(location).size();
			this.score = this.count / (double) wordCount.get(location);
		}

		@Override
		public int compareTo(Result original) {
			int scoreComparison = Double.compare(original.getScore(), this.getScore());
			if (scoreComparison != 0) {
				return scoreComparison;
			} else {
				int countComparison = Integer.compare(original.getCount(), this.getCount());
				if (countComparison < 0) {
					return countComparison;
				} else {
					return this.getLocation().compareToIgnoreCase(original.getLocation());
				}
			}
		}
	}
}
