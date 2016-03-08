/*
 * Copyright (C) 2015 Edmund Klaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.objectpocket.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.objectpocket.Blob;
import org.objectpocket.util.JsonHelper;

import com.google.gson.Gson;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class FileStore implements ObjectStore {

	private static final String JSON_PREFIX = "{\"objects\":[";
	private static final String JSON_PREFIX_REGEX_PATTERN = "\\{\\s*\"objects\"\\s*:\\s*\\[";
	private static final String JSON_SUFFIX = "]}";
	private static final String JSON_SUFFIX_REGEX_PATTERN_REVERSED = "\\s*\\}\\s*\\]";
	
	private String directory;
	private static final String BLOB_STORE_DIRNAME = "blobstore";
	
	private final String INDEX_FILE_NAME = ".op_index";
	private ObjectPocketIndex objectPocketIndex = new ObjectPocketIndex();

	public FileStore(String directory) {
		this.directory = directory;
	}

	@Override
	public Set<String> getAvailableObjectTypes() throws IOException {
		readIndexFile();
		return objectPocketIndex.getTypeToFilenamesMapping().keySet();
	}

	@Override
	public Map<String,String> readJsonObjects(String typeName) throws IOException {
		if (typeName == null) {
			return null;
		}
		Set<String> filenames = objectPocketIndex.getTypeToFilenamesMapping().get(typeName);
		Map<String, String> objects = new HashMap<String, String>();
		for (String filename : filenames) {
			File file = initFile(filename, true, false);
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				long time = System.currentTimeMillis();
				String line = null;
				StringBuffer objectBuffer = new StringBuffer();
				while((line = br.readLine()) != null) {
					line = line.trim();
					if (!line.isEmpty()) {
						objectBuffer.append(line);
					}
				}
				String s = objectBuffer.toString();
				//System.out.println(System.currentTimeMillis() - time);
				s = s.replaceFirst(JSON_PREFIX_REGEX_PATTERN, "");
				// TODO: this is maybe faster with regex?? \\]\\s*\\}\\s*
				s = StringUtils.reverse(s);
				s = s.replaceFirst(JSON_SUFFIX_REGEX_PATTERN_REVERSED, "");
				s = StringUtils.reverse(s);
				//System.out.println(System.currentTimeMillis() - time);
				List<String> jsonStrings = JsonHelper.splitToTopLevelJsonObjects(s);
				//System.out.println(System.currentTimeMillis() - time);
				for (int i = 0; i < jsonStrings.size(); i++) {
					String[] classAndIdFromJson = JsonHelper.getClassAndIdFromJson(jsonStrings.get(i));
					if (classAndIdFromJson[0].equals(typeName)) {
						objects.put(classAndIdFromJson[1], jsonStrings.get(i));
					}
				}
				//System.out.println(System.currentTimeMillis() - time);
			} catch (IOException e) {
				throw new IOException("Could not read from file. " + file.getPath(), e);
			}
		}
		return objects;
	}

	@Override
	public void writeJsonObjects(Map<String, Set<String>> jsonObjects, String typeName) throws IOException {
		readIndexFile();
		for (String filename : jsonObjects.keySet()) {
			File file = initFile(filename, true, true);
			try (FileWriter fw = new FileWriter(file)) {
				addToIndex(typeName, filename);
				fw.write(JSON_PREFIX + "\n");
				Set<String> objectSet = jsonObjects.get(filename);
				Iterator<String> iterator = objectSet.iterator();
				while(iterator.hasNext()) {
					fw.write(iterator.next());
					if (iterator.hasNext()) {
						fw.write(",");
					}
					fw.write("\n");
				}
				fw.write(JSON_SUFFIX);
			} catch (IOException e) {
				throw new IOException("Could not write to file. " + file.getPath(), e);
			}
		}
		writeIndexFile();
	}

	@Override
	public void writeBlobs(Set<Blob> blobs) throws IOException {
		File blobStore = initBlobStore();
		for (Blob blob : blobs) {
			String path = blob.getPath();
			if (path == null || path.trim().isEmpty()) {
				path = blob.getId();
			}
			// find/create path and write blob
			path = path.replaceAll("\\\\", "/");
			String name = null;
			File dir = null;
			if (path.contains("/")) {
				name = path.substring(path.lastIndexOf("/"));
				path = path.substring(0, path.lastIndexOf("/"));
				while(path.startsWith("/")) {
					path = path.substring(1, path.length());
				}
				dir = new File(blobStore.getPath() + File.separatorChar + path);
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						throw new IOException("Could not create directory for blob. " + dir.getPath());
					}
				}
			} else {
				name = path;
				dir = new File(blobStore.getPath());
			}
			File f = new File(dir.getPath() + File.separatorChar + name);
			try (FileOutputStream fOut = new FileOutputStream(f)) {
				fOut.write(blob.getBytes());
			}
		}
	}

	@Override
	public byte[] loadBlobData(Blob blob) throws IOException {
		File blobStore = initBlobStore();
		String path = blob.getPath();
		if (path == null || path.trim().isEmpty()) {
			path = blob.getId();
		}
		File f = new File(blobStore.getPath() + File.separatorChar + path);
		try (FileInputStream fIn = new FileInputStream(f)) {
			byte[] bytes = new byte[(int)f.length()];
			fIn.read(bytes);
			return bytes;
		}
	}

	@Override
	public String getSource() {
		return directory;
	}

	private File initFile(String typeName, boolean read, boolean write) throws IOException {
		File dir = initFileStore();
		String filename = dir.getPath() + File.separatorChar + typeName;
		if (!typeName.equals(INDEX_FILE_NAME)) {
			filename += ".json";
		}
		File f = new File(filename);
		if (!f.exists()) {
			try {
				f.createNewFile();
				if (typeName.equals(INDEX_FILE_NAME)) {
					Logger.getAnonymousLogger().warning("Could not find index file. Will create.");
					writeIndexFileData(f);
				}
			} catch (IOException e) {
				throw new IOException("File could not be created. " + filename, e);
			}
		}
		if (read && !f.canRead()) {
			throw new IOException("File is not readable. " + filename);
		}
		if (write && !f.canWrite()) {
			throw new IOException("File is not writeable. " + filename);
		}
		return f;
	}

	private File initFileStore() throws IOException {
		File dir = new File(directory);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (!dir.exists()) {
			throw new IOException("File store does not exist. " + directory);
		}
		if (!dir.isDirectory()) {
			throw new IOException("File store is not a directory. " + directory);
		}
		return dir;
	}

	private File initBlobStore() throws IOException {
		File fileStore = initFileStore();
		File dir = new File(fileStore.getPath() + File.separatorChar + BLOB_STORE_DIRNAME);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				throw new IOException("Blob store could not be created. " + dir.getPath());
			}
		}
		if (!dir.isDirectory()) {
			throw new IOException("Blob stire is not a directory. " + dir.getPath());
		}
		return dir;
	}
	
	private void addToIndex(String typeName, String filename) {
		if (objectPocketIndex.getTypeToFilenamesMapping().get(typeName) == null) {
			objectPocketIndex.getTypeToFilenamesMapping().put(typeName, new HashSet<String>());
		}
		objectPocketIndex.getTypeToFilenamesMapping().get(typeName).add(filename);
	}
	
	private void readIndexFile() throws IOException {
		File file = initFile(INDEX_FILE_NAME, true, false);
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			line = br.readLine();
		} catch (IOException e) {
			throw new IOException("Could not read index file. " + file.getPath(), e);
		}
		if (line != null) {
			Gson gson = new Gson();
			ObjectPocketIndex o = gson.fromJson(line, ObjectPocketIndex.class);
			if (o != null) {
				objectPocketIndex = o;
				return;
			}
		}
		throw new IOException("Could not parse index file data to index object. " + file.getPath());
	}
	
	private void writeIndexFile() throws IOException {
		File file = initFile(INDEX_FILE_NAME, true, false);
		writeIndexFileData(file);
	}
	
	private void writeIndexFileData(File file) throws IOException {
		try (FileWriter fw = new FileWriter(file)) {
			Gson gson = new Gson();
			String jsonString = gson.toJson(objectPocketIndex);
			fw.write(jsonString);
		} catch (IOException e) {
			throw new IOException("Could not write index file. " + file.getPath(), e);
		}
	}

}
