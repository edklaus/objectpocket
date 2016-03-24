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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
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
import java.util.zip.ZipEntry;

import org.objectpocket.Blob;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ZipBlobStore implements BlobStore {

	private static final String BLOB_STORE_FILENAME = "binary";
	
	Map<String, String> env = new HashMap<>(); 
    URI uri = null; 
	
	public ZipBlobStore(String directory) {
		env.put("create", "true");
		Path path = Paths.get(directory + "/" + BLOB_STORE_FILENAME);
		uri = URI.create("jar:" + path.toUri());
	}
	
	@Override
	public void writeBlobs(Set<Blob> blobs) throws IOException {
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
					while(path.startsWith("/")) {
						path = path.substring(1, path.length());
					}
					Path pathInZip = fs.getPath(path);
					if (!Files.exists(pathInZip)) {
						Files.createDirectories(pathInZip);
					}
					if (!Files.exists(pathInZip)) {
						throw new IOException("Could not create directory for blob. " + path);
					}
					name = path + name;
				}
				Path fileInZip = fs.getPath(name);
				try (OutputStream out = Files.newOutputStream(fileInZip, StandardOpenOption.CREATE)) {
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
		try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
			Path fileInZip = fs.getPath(path);
			byte[] buf = new byte[1024];
			List<Byte> bytes = new ArrayList<Byte>(1024000); // 1 MB
			try (InputStream in = Files.newInputStream(fileInZip)) {
				int length = -1;
				while((length = in.read(buf)) != -1) {
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
	}

}
