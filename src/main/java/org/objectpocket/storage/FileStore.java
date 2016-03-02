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
import java.util.Map;
import java.util.Set;

import org.objectpocket.Blob;
import org.objectpocket.util.JsonHelper;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class FileStore implements ObjectStore {

	private String directory;
	private static final String BLOB_STORE_DIRNAME = "blobstore";

	public FileStore(String directory) {
		this.directory = directory;
	}

	@Override
	public Set<String> getAvailableObjectTypes() throws IOException {
		// FIXME: change implementation to use an overview file
		// where type->file mapping is listed
		// make it resistant against losing this mapping file
		File dir = initFileStore();
		File[] list = dir.listFiles();
		if (list != null && list.length > 0) {
			Set<String> set = new HashSet<String>(list.length);
			for (File file : list) {
				if (!file.isDirectory()) {
					set.add(file.getName());
				}
			}
			return set;
		}
		return null;
	}

	@Override
	public Map<String,String> readJsonObjects(String typeName) throws IOException {
		if (typeName == null) {
			return null;
		}
		File file = initFile(typeName, true, false);
		Map<String, String> objects = null;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = null;
			objects = new HashMap<String, String>();
			StringBuffer objectBuffer = new StringBuffer();
			while((line = br.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					objectBuffer.append(line);
				}
			}
			String s = objectBuffer.toString();
			String[] jsonStrings = s.split("\\}\n*\\{");
			for (int i = 0; i < jsonStrings.length; i++) {
				String jsonString = jsonStrings[i];
				if (i > 0) {
					jsonString = "{" + jsonString;
				}
				if (i < jsonStrings.length-1) {
					jsonString = jsonString + "}";
				}
				String[] classAndIdFromJson = JsonHelper.getClassAndIdFromJson(jsonString);
				objects.put(classAndIdFromJson[1], jsonString);
			}
		} catch (IOException e) {
			throw new IOException("Could not read from file. " + file.getPath(), e);
		}
		return objects;
	}

	@Override
	public void writeJsonObjects(Set<String> jsonObjects, String typeName) throws IOException {
		if (typeName == null || typeName.isEmpty()) {
			return;
		}
		File file = initFile(typeName, true, true);
		if (jsonObjects == null || jsonObjects.isEmpty()) {
			file.delete();
			return;
		}
		try (FileWriter fw = new FileWriter(file)) {
			for (String string : jsonObjects) {
				fw.write(string + "\n");
			}
		} catch (IOException e) {
			throw new IOException("Could not write to file. " + file.getPath(), e);
		}
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
		File f = new File(filename);
		if (!f.exists()) {
			try {
				f.createNewFile();
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

}
