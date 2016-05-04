/*
 * Copyright (C) 2016 Edmund Klaus
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

package org.objectpocket.storage.blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.objectpocket.Blob;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ZipBlobStore implements BlobStore {

    public static final String BLOB_STORE_DEFAULT_FILENAME = "binary";

    private String directory;
    private String filename;
    private Path path;
    private URI uri = null;
    private FileSystem fsRead = null;

    public ZipBlobStore(String directory) {
	this(directory, BLOB_STORE_DEFAULT_FILENAME);
    }

    protected ZipBlobStore(String directory, String filename) {
	this.directory = directory;
	this.filename = filename;
	path = Paths.get(directory + "/" + filename);
	uri = URI.create("jar:" + path.toUri());
    }

    private void openReadFileSystem() throws IOException {
	Map<String, String> env = new HashMap<>();
	fsRead = FileSystems.newFileSystem(uri, env);
    }

    @Override
    public void writeBlobs(Set<Blob> blobs) throws IOException {
	if (blobs == null || blobs.isEmpty()) {
	    return;
	}
	Map<String, String> env = new HashMap<>();
	env.put("create", "true");
	try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
	    for (Blob blob : blobs) {
		String path = blob.getPath();
		if (path == null || path.trim().isEmpty()) {
		    path = blob.getId();
		}
		// find/create path and write blob
		path = path.replaceAll("\\\\", "/");
		String name = path;
		if (path.contains("/")) {
		    name = path.substring(path.lastIndexOf("/"));
		    path = path.substring(0, path.lastIndexOf("/"));
		    while (path.startsWith("/")) {
			path = path.substring(1, path.length());
		    }
		    Path pathInZip = fs.getPath(path);
		    if (!Files.exists(pathInZip)) {
			Files.createDirectories(pathInZip);
		    }
		    if (!Files.exists(pathInZip)) {
			throw new IOException(
				"Could not create directory for blob. " + path);
		    }
		    name = path + name;
		}
		Path fileInZip = fs.getPath(name);
		try (OutputStream out = Files.newOutputStream(fileInZip,
			StandardOpenOption.CREATE)) {
		    out.write(blob.getBytes());
		}
	    }
	}

    }

    @Override
    public byte[] loadBlobData(Blob blob) throws IOException {
	String path = blob.getPath();
	if (path == null || path.trim().isEmpty()) {
	    path = blob.getId();
	}
	if (fsRead == null || !fsRead.isOpen()) {
	    openReadFileSystem();
	}
	Path fileInZip = fsRead.getPath(path);
	byte[] buf = new byte[1024];
	List<Byte> bytes = new ArrayList<Byte>(1024000); // 1 MB
	try (InputStream in = Files.newInputStream(fileInZip)) {
	    int length = -1;
	    while ((length = in.read(buf)) != -1) {
		for (int i = 0; i < length; i++) {
		    bytes.add(buf[i]);
		}
	    }
	}
	buf = new byte[bytes.size()];
	for (int i = 0; i < bytes.size(); i++) {
	    buf[i] = bytes.get(i);
	}
	return buf;
    }

    @Override
    public void cleanup(Set<Blob> referencedBlobs) throws IOException {
	if (referencedBlobs == null) {
	    return;
	}

	ZipBlobStore newZip = new ZipBlobStore(directory, this.filename
		+ "_tmp");
	newZip.writeBlobs(referencedBlobs);
	newZip.close();
	this.close();
	Files.delete(this.path);
	File f = new File(this.directory + "/" + this.filename + "_tmp");
	f.renameTo(this.path.toFile());

	// // create temp zip
	// Path path = Paths.get(directory + "/" + BLOB_STORE_FILENAME +
	// "_tmp");
	// URI uri = URI.create("jar:" + path.toUri());
	// Map<String, String> env = new HashMap<>();
	// env.put("create", "true");
	// try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
	// // transfer referenced blobs
	// openReadFileSystem();
	// for (Blob blob : referencedBlobs) {
	// String pathString = blob.getPath();
	// if (pathString == null || pathString.trim().isEmpty()) {
	// pathString = blob.getId();
	// }
	// }
	// close();
	// }

	// replace old zip with temp zip

	// TODO:
	// Was ist an dieser Stelle einfacher?
	// 1. Löschen?
	// 2. Neues Zip anlegen?
    }

    @Override
    public void close() throws IOException {
	if (fsRead != null && fsRead.isOpen()) {
	    fsRead.close();
	}
    }

    @Override
    public void delete() throws IOException {
	if (fsRead != null) {
	    if (fsRead.isOpen()) {
		fsRead.close();
	    }
	}
	Files.delete(path);
    }

    public long numEntries() throws IOException {
	ZipFile zipFile = new ZipFile(path.toFile());
	int returnVal = zipFile.size();
	zipFile.close();
	return returnVal;
    }

}
