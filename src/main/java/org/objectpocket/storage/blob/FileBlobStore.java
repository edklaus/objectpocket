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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.objectpocket.Blob;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class FileBlobStore implements BlobStore {

    private String directory;
    public static final String BLOB_STORE_DIRNAME = "binary";

    public FileBlobStore(String directory) {
	this.directory = directory;
    }

    @Override
    public void writeBlobs(Set<Blob> blobs) throws IOException {
	if (blobs == null || blobs.isEmpty()) {
	    return;
	}
	File blobStore = initBlobStore(true);
	for (Blob blob : blobs) {
	    String path = blob.getPath();
	    if (path == null || path.isEmpty()) {
		path = blob.getId();
	    }
	    // find/create path and write blob
	    path = path.replaceAll("\\\\", "/");
	    String name = null;
	    File dir = null;
	    if (path.contains("/")) {
		name = path.substring(path.lastIndexOf("/"));
		path = path.substring(0, path.lastIndexOf("/"));
		while (path.startsWith("/")) {
		    path = path.substring(1, path.length());
		}
		dir = new File(blobStore.getPath() + File.separatorChar + path);
		if (!dir.exists()) {
		    if (!dir.mkdirs()) {
			throw new IOException(
				"Could not create directory for blob. "
					+ dir.getPath());
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
	    blob.setPersisted();
	}
    }

    @Override
    public byte[] loadBlobData(Blob blob) throws IOException {
	File blobStore = initBlobStore(false);
	String path = blob.getPath();
	if (path == null || path.trim().isEmpty()) {
	    path = blob.getId();
	}
	File f = new File(blobStore.getPath() + File.separatorChar + path);
	try (FileInputStream fIn = new FileInputStream(f)) {
	    byte[] bytes = new byte[(int) f.length()];
	    fIn.read(bytes);
	    return bytes;
	}
    }

    private File initBlobStore(boolean write) throws IOException {
	File dir = new File(directory + "/" + BLOB_STORE_DIRNAME);
	if (!dir.exists()) {
	    if (write) {
		if (!dir.mkdirs()) {
		    throw new IOException("Blob store could not be created. "
			    + dir.getPath());
		}
	    } else {
		throw new IOException(
			"Blob store does not exist. Nothing to read here.");
	    }
	}
	if (!dir.isDirectory()) {
	    throw new IOException("Blob store is not a directory. "
		    + dir.getPath());
	}
	return dir;
    }

    @Override
    public void cleanup(Set<Blob> referencedBlobs) throws IOException {
	if (referencedBlobs == null) {
	    return;
	}
	File dir = new File(directory + "/" + BLOB_STORE_DIRNAME);
	Set<String> paths = new HashSet<>(referencedBlobs.size());
	for (Blob blob : referencedBlobs) {
	    String path = blob.getPath();
	    if (path == null || path.isEmpty()) {
		path = blob.getId();
	    }
	    paths.add(path.replaceAll("\\\\", "/"));
	}
	initBlobStore(false);
	Collection<File> files = FileUtils.listFiles(dir,
		FileFilterUtils.fileFileFilter(), TrueFileFilter.INSTANCE);
	for (File file : files) {
	    String path = file.getPath().replace(dir.getPath(), "");
	    path = path.replaceAll("\\\\", "/");
	    while (path.startsWith("/")) {
		path = path.substring(1);
	    }
	    if (!paths.contains(path)) {
		File f = new File(directory + "/" + BLOB_STORE_DIRNAME + "/"
			+ path);
		f.delete();
	    }
	}
	// clear empty dirs
	Collection<File> dirs = FileUtils.listFilesAndDirs(dir,
		DirectoryFileFilter.DIRECTORY, DirectoryFileFilter.DIRECTORY);
	for (File d : dirs) {
	    if (d.list().length == 0) {
		d.delete();
	    }
	}
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void delete() throws IOException {
	initBlobStore(false);
	File dir = new File(directory + "/" + BLOB_STORE_DIRNAME);
	FileUtils.deleteDirectory(dir);
    }

    public long numEntries() throws IOException {
	File dir = new File(directory + "/" + BLOB_STORE_DIRNAME);
	Collection<File> files = FileUtils.listFiles(dir,
		FileFilterUtils.fileFileFilter(), TrueFileFilter.INSTANCE);
	return files.size();
    }
}
