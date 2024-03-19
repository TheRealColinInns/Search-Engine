import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Class responsible for navigating the directories found in the command line
 * arguments.
 *
 * @author CS 212 Software Development - Colin Inns
 * @author University of San Francisco
 * @version Spring 2021
 */
public class DirectoryNavigator {
	/**
	 * Traverses through the directory and its subdirectories, outputting all paths
	 * to an array list filled with all the paths
	 *
	 * @param start      the initial path to traverse
	 * @param pathsFound an array list full of all of the paths found
	 * @throws IOException if an I/O error occurs
	 */
	public static void findPaths(Path start, ArrayList<Path> pathsFound) throws IOException {
		// use the Files class to get information about a path
		if (Files.isDirectory(start)) {
			// output trailing slash to indicate directory
			// start directory traversal
			traverseDirectory(start, pathsFound);
		} else if (isTextFile(start)) {
			pathsFound.add(start);
		}
	}

	/**
	 * Method that helps original findPaths recursively find the paths
	 * 
	 * @param start the starting path
	 * @return the list of paths
	 * @throws IOException to catch in driver
	 */
	public static ArrayList<Path> findPaths(Path start) throws IOException {
		ArrayList<Path> paths = new ArrayList<Path>();
		findPaths(start, paths);
		return paths;
	}

	/**
	 * Checks if text file
	 *
	 * @param inputFile the path to check if it is a text file
	 * @return {@code true} if the path is text file
	 */
	public static boolean isTextFile(Path inputFile) {
		String lower = inputFile.toString().toLowerCase();
		return lower.endsWith(".txt") || lower.endsWith(".text");
	}

	/**
	 * Traverses through the directory and its subdirectories, outputting all paths
	 * back into the finds path method
	 *
	 * @param directory  the directory to traverse
	 * @param pathsFound the data structure in which the data is stored
	 * @throws IOException if an I/O error occurs
	 */
	private static void traverseDirectory(Path directory, ArrayList<Path> pathsFound) throws IOException {
		/*
		 * The try-with-resources block makes sure we close the directory stream when
		 * done, to make sure there aren't any issues later when accessing this
		 * directory.
		 *
		 * Note, however, we are still not catching any exceptions. This type of try
		 * block does not have to be accompanied with a catch block. (You should,
		 * however, do something about the exception.)
		 */
		try (DirectoryStream<Path> myDirectoryStream = Files.newDirectoryStream(directory)) {
			// use an enhanced-for or for-each loop for efficiency and simplicity
			for (Path temporaryPath : myDirectoryStream) {
				findPaths(temporaryPath, pathsFound);
			}
		}
	}
}
