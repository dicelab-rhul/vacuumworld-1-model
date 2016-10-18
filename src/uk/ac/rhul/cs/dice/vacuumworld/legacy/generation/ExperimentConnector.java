package uk.ac.rhul.cs.dice.vacuumworld.legacy.generation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class ExperimentConnector {

	private static final String PATH_KEY = "path";
	private static final String SIZES_KEY = "sizes";
	private static final String AGENTS_KEY = "agents";
	private static final String CONFIG_FILE = "TestConfig.txt";

	private String path;
	private int[] sizes;
	private int[] agents;

	private GenerationSequence gen = null;

	public ExperimentConnector() throws ConfigFileException {
		File config = getConfigFile();
		List<String> lines = new ArrayList<>();
		
		try (Stream<String> stream = Files.lines(Paths.get(config.getAbsolutePath()))) {
			stream.forEach((String s) -> handle(lines, s));
		}
		catch (Exception e) {
			Utils.log(e);
		}
	}

	public void handle(List<String> lines, String line) {
		try {
			lines.add(line);
			handleLine(line);
		}
		catch (Exception e) {
			Utils.log(e);
		}
	}
	
	public void generateTestFiles() throws ConfigFileException {
		File p = new File(this.path);
		
		if (!p.exists()) {
			create(p);
		}
		
		this.gen = new GenerationSequence(this.path + "/", this.sizes, this.agents);
		this.gen.generateAllTestCases();
	}

	private void create(File p) throws ConfigFileException {
		if (!p.mkdir()) {
			throw new ConfigFileException(ExperimentConnector.CONFIG_FILE, "Cannot create dir: " + this.path);
		}
	}

	private void handleLine(String line) throws ConfigFileException {
		String[] split = line.split(":");
		
		if (split.length != 2) {
			throw new ConfigFileException(ExperimentConnector.CONFIG_FILE, "Invalid line structure: " + line);
		}
		
		String com = split[0].trim();
		handleLineHelper(split, com);
	}

	private void handleLineHelper(String[] split, String com) throws ConfigFileException {
		if (PATH_KEY.equals(com)) {
			handlePath(split[1]);
		}
		else if (SIZES_KEY.equals(com)) {
			this.sizes = handleArray(split[1]);
		}
		else if (AGENTS_KEY.equals(com)) {
			this.agents = handleArray(split[1]);
		}
		else {
			throw new ConfigFileException(ExperimentConnector.CONFIG_FILE, "Invalid property: " + com);
		}
	}

	private void handlePath(String line) {
		this.path = line.replaceAll("\\s+", "");
	}

	private int[] handleArray(String line) throws ConfigFileException {
		String tmp = line.replaceAll("\\s+", "");
		String[] split = tmp.split(",");
		
		if (split.length < 1) {
			throw new ConfigFileException(ExperimentConnector.CONFIG_FILE, "Invalid number of arguments");
		}
		
		return handleArrayHelper(split);
	}

	private int[] handleArrayHelper(String[] split) {
		int[] array = new int[split.length];
		
		for (int i = 0; i < split.length; i++) {
			array[i] = Integer.valueOf(split[i]);
		}
		return array;
	}

	private File getConfigFile() throws ConfigFileException {
		File config = new File(CONFIG_FILE);
		
		if (!config.exists()) {
			throw new ConfigFileException(ExperimentConnector.CONFIG_FILE, "Config file does not exist");
		}
		
		return config;
	}

	public Set<File> getFilePaths() throws ConfigFileException {
		if (this.gen == null) {
			Set<File> files = new HashSet<>();
			recurseFileStructure(new File(this.path + "/"), files);
			logPaths(files);
			
			return files;

		}
		else {
			return this.gen.getCompleteFilePaths();
		}
	}

	private void logPaths(Set<File> files) {
		for(File file : files) {
			Utils.logWithClass(this.getClass().getSimpleName(), "File: " + file.getPath() + "...");
		}
	}

	public void recurseFileStructure(File folder, Set<File> files) {
		Set<File> folders = new HashSet<>();
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			files.add(listOfFiles[i]);
		}
		
		separateFilesAndFolders(files, folders);
		files.removeAll(folders);
		doRecursion(folders, files);
	}

	private void doRecursion(Set<File> folders, Set<File> files) {
		for(File folder : folders) {
			recurseFileStructure(folder, files);
		}
	}

	private void separateFilesAndFolders(Set<File> files, Set<File> folders) {
		for(File file : files) {
			if (file.isFile()) {
				files.add(file);
			}
			else if (file.isDirectory()) {
				folders.add(file);
			}
		}
	}
}