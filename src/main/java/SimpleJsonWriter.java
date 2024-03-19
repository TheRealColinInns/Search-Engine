import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 212 Software Development - Colin Inns
 * @author University of San Francisco
 * @version Spring 2021
 */
public class SimpleJsonWriter {

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
		writer.write("[\n");
		level++;
		Iterator<Integer> elementsIterator = elements.iterator();
		if (elementsIterator.hasNext()) {
			indent(elementsIterator.next().toString(), writer, level);
		}
		while (elementsIterator.hasNext()) {
			writer.write(",\n");
			indent(elementsIterator.next().toString(), writer, level);
		}
		writer.write("\n");
		level--;
		indent("]", writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		indent("{", writer, level);
		level++;
		String next;
		Iterator<String> keyIterator = elements.keySet().iterator();
		if (keyIterator.hasNext()) {
			writer.write("\n");
			next = keyIterator.next();
			indent("\"" + next + "\": " + elements.get(next), writer, level);
		}
		while (keyIterator.hasNext()) {
			writer.write(",\n");
			next = keyIterator.next();
			indent("\"" + next + "\": " + elements.get(next), writer, level);
		}
		level--;
		indent("\n}", writer, level);
	}

	/**
	 * Helps write the elements as a pretty JSON object with a nested array. The
	 * generic notation used allows this method to be used for any type of map with
	 * any type of nested collection of integer objects.
	 *
	 * @param nested the elements to write
	 * @param writer the writer to use
	 * @param level  the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedMap(Map<String, ? extends Collection<Integer>> nested, Writer writer, int level)
			throws IOException {
		writer.write("{\n");
		level++;
		Iterator<String> pathIterator = nested.keySet().iterator();
		String pathNext;
		if (pathIterator.hasNext()) {
			pathNext = pathIterator.next();
			indent("\"" + pathNext + "\": ", writer, level);
			asArray(nested.get(pathNext), writer, level);
		}
		while (pathIterator.hasNext()) {
			writer.write(",\n");
			pathNext = pathIterator.next();
			indent("\"" + pathNext + "\": ", writer, level);
			asArray(nested.get(pathNext), writer, level);
		}
		writer.write("\n");
		level--;
		indent("}", writer, level);

	}

	/**
	 * Writes the elements as a pretty JSON object with a nested array. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Map<String, ? extends Collection<Integer>>> elements,
			Writer writer, int level) throws IOException {
		indent("{", writer, level);
		level++;
		String wordNext;
		Iterator<String> wordIterator = elements.keySet().iterator();
		if (wordIterator.hasNext()) {
			writer.write("\n");
			wordNext = wordIterator.next();
			indent("\"" + wordNext + "\": ", writer, level);
			asNestedMap(elements.get(wordNext), writer, level);
		}
		while (wordIterator.hasNext()) {
			writer.write(",\n");
			wordNext = wordIterator.next();
			indent("\"" + wordNext + "\": ", writer, level);
			asNestedMap(elements.get(wordNext), writer, level);
		}
		writer.write("\n}");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static void asNestedArray(Map<String, ? extends Map<String, ? extends Collection<Integer>>> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a nested pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static String asNestedArray(Map<String, ? extends Map<String, ? extends Collection<Integer>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(String element, Writer writer, int level) throws IOException {
		writer.write("\t".repeat(level));
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void quote(String element, Writer writer, int level) throws IOException {
		writer.write("\t".repeat(level));
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * adds a single query
	 * 
	 * @param results the results we are pulling from
	 * @param query   the query we are searching for
	 * @param index   the index in the list of results
	 * @param writer  where we are writing to
	 * @param level   the indent level
	 * @throws IOException if we can't write
	 */
	public static void asSingleQueryWord(TreeMap<String, List<InvertedIndex.Result>> results, String query, int index, Writer writer, int level)
			throws IOException {
		DecimalFormat FORMATTER = new DecimalFormat("0.00000000");
		indent("{\n", writer, level);

		level++;
		
		indent("\"where\": \"" + results.get(query).get(index).getLocation() + "\",\n", writer, level);
		indent("\"count\": " + results.get(query).get(index).getCount() + ",\n", writer, level);
		indent("\"score\": " + FORMATTER.format(results.get(query).get(index).getScore()), writer, level);

		level--;
		writer.write("\n");
		indent("}", writer, level);
	}

	/**
	 * writes a single query
	 * 
	 * @param results the results we are pulling from
	 * @param query   the search query
	 * @param writer  where we are writing to
	 * @param level   the indent level
	 * @throws IOException if we can't write
	 */
	public static void asSingleQuery(TreeMap<String, List<InvertedIndex.Result>> results, String query, Writer writer, int level) throws IOException {
		writer.write("[");
		level++;
		if (results.get(query).size()>0) {
			writer.write("\n");
			asSingleQueryWord(results, query, 0, writer, level);
		}
		for (int i = 1; i < results.get(query).size(); i++) {
			writer.write(",\n");
			asSingleQueryWord(results, query, i, writer, level);
		}
		level--;
		writer.write("\n");
		indent("]", writer, level);
	}

	/**
	 * writes the results of the search
	 * 
	 * @param results the results of the search to write
	 * @param writer  the writer we write to
	 * @param level   indent level
	 * @throws IOException if we cant write to the file
	 */
	public static void asSearchResult(TreeMap<String, List<InvertedIndex.Result>> results, Writer writer, int level) throws IOException {
		writer.write("{");
		level++;
		Iterator<String> queryIterator = results.keySet().iterator();
		if (queryIterator.hasNext()) {
			String query = queryIterator.next();
			writer.write("\n");
			indent("\"" + query + "\": ", writer, level);
			asSingleQuery(results, query, writer, level);
		}
		while (queryIterator.hasNext()) {
			String query = queryIterator.next();
			writer.write(",\n");
			indent("\"" + query + "\": ", writer, level);
			asSingleQuery(results, query, writer, level);
		}
		writer.write("\n");
		level--;
		indent("}", writer, level);

	}

	/**
	 * the method that writes specifically to a file
	 * 
	 * @param results the results of the search
	 * @param output  the file we are writing to
	 * @throws IOException throws if we can't write
	 */
	public static void asSearchResult(TreeMap<String, List<InvertedIndex.Result>> results, Path output) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
			asSearchResult(results, writer, 0);
		}
	}

	/**
	 * writes to a string
	 * 
	 * @param results the results to the search
	 * @return a string of the write
	 */
	public static String asSearchResult(TreeMap<String, List<InvertedIndex.Result>> results) {
		try {
			StringWriter writer = new StringWriter();
			asSearchResult(results, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

}
