package uk.ac.rhul.cs.dice.vacuumworld.legacy.generation;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class GenerationSequence {

	private int[] sizes;
	private int[] agents;

	private Map<Integer, String> directoryMap = new HashMap<>();
	private Map<Integer, String> fileNameMap = new HashMap<>();
	private Set<File> completeFilePaths = new HashSet<>();
	private String filePath;

	public GenerationSequence(String filePath, int[] sizes, int[] agents) {
		this.sizes = sizes;
		this.agents = agents;
		this.filePath = filePath;
		
		generateDirectories(filePath);
	}

	public void generateAllTestCases() {
		Generator gen = new Generator();
		
		for (int i = 0; i < this.sizes.length; i++) {
			for (int j = 0; j < agents.length; j++) {
				generateTestCase(gen, i, j);
			}
		}
	}

	private void generateTestCase(Generator gen, int i, int j) {
		if (this.agents[j] < this.sizes[i] * this.sizes[i]) {
			generateTestCase(gen, this.filePath, this.agents[j], this.sizes[i]);
		}
	}

	private void generateTestCase(Generator gen, String filePath, int agents, int size) {
		File f = new File(filePath + getDirName(size) + getFileName(agents));
		this.completeFilePaths.add(f);
		gen.generate(f, size, size, agents, calculateNumDirts(size));
	}

	private void generateDirectories(String path) {
		Utils.logWithClass(this.getClass().getSimpleName(), "generating directories...");
		
		generateDirectoriesHelper(path);
		generateFilesNames();
	}

	
	
	private void generateDirectoriesHelper(String path) {
		for (int i = 0; i < this.sizes.length; i++) {
			File file = new File(path + generateDirName(this.sizes[i]));
			
			if (!file.exists()) {
				file.mkdir();
			}
		}
	}

	private void generateFilesNames() {
		for (int j = 0; j < this.agents.length; j++) {
			generateFileName(this.agents[j]);
		}
	}

	private String generateFileName(int agents) {
		String name = "A" + agents + ".json";
		this.fileNameMap.put(agents, name);
		
		return name;
	}

	private String getFileName(int agents) {
		return this.fileNameMap.get(agents);
	}

	private String getDirName(int size) {
		return this.directoryMap.get(size);
	}

	private String generateDirName(int size) {
		String dir = "D" + size + "x" + size + "/";
		this.directoryMap.put(size, dir);
		
		return dir;
	}

	private int calculateNumDirts(int size) {
		return (int) (Math.ceil(0.1 * size));
	}

	public Set<File> getCompleteFilePaths() {
		return this.completeFilePaths;
	}
}