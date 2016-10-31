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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.objectpocket.Blob;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class MultiZipBlobStore implements BlobStore {

    public static final String BLOB_STORE_DEFAULT_FILENAME = "binary";
    public static final long MAX_BINARY_FILE_SIZE = 51200000; // 50 MB

    private String directory;
    private File lastBlobContainer;
    private int lastBlobContainerNum = -1;
    private long lastBlobContainerSize = 0;

    private Map<String, FileSystem> readFileSystems = new HashMap<>();
    private Map<String, String> blobContainerIndex;

    public MultiZipBlobStore(String directory) {
        this.directory = directory;
    }

    @Override
    public void writeBlobs(Set<Blob> blobs) throws IOException {
        if (blobs == null || blobs.isEmpty()) {
            return;
        }
        scanForBlobContainers();
        if (blobContainerIndex == null) {
            createIndexFromBlobContainers();
        }
        FileSystem currentWriteFileSystem = null;
        for (Blob blob : blobs) {

            if (lastBlobContainer == null || blob.getBytes().length + lastBlobContainerSize > MAX_BINARY_FILE_SIZE) {
                if (currentWriteFileSystem != null) {
                    currentWriteFileSystem.close();
                    currentWriteFileSystem = null;
                }
                createNextBinary();
            }

            if (currentWriteFileSystem == null) {
                closeReadFileSystem(lastBlobContainer.getName());
                Path path = Paths.get(lastBlobContainer.getAbsolutePath());
                URI uri = URI.create("jar:" + path.toUri());
                Map<String, String> env = new HashMap<>();
                env.put("create", "true");
                currentWriteFileSystem = FileSystems.newFileSystem(uri, env);
            }

            // persist blob data
            String path = blob.getPath();
            if (path == null || path.trim().isEmpty()) {
                path = blob.getId();
            }
            // find/create path and write blob
            path = path.replaceAll("\\\\", "/");

            // FIXME: Das berücksichtigt keine Änderungen an einer Binary!!
            String name = path;
            if (path.contains("/")) {
                name = path.substring(path.lastIndexOf("/"));
                path = path.substring(0, path.lastIndexOf("/"));
                while (path.startsWith("/")) {
                    path = path.substring(1);
                }
                Path pathInZip = currentWriteFileSystem.getPath(path);
                if (!Files.exists(pathInZip)) {
                    Files.createDirectories(pathInZip);
                }
                if (!Files.exists(pathInZip)) {
                    throw new IOException("Could not create directory for blob. " + path);
                }
                path = path + name;
            }
            Path fileInZip = currentWriteFileSystem.getPath(path);
            try (OutputStream out = Files.newOutputStream(fileInZip, StandardOpenOption.CREATE)) {
                out.write(blob.getBytes());
            }
            blobContainerIndex.put(path, lastBlobContainer.getName());
            blob.setPersisted();
            lastBlobContainerSize = lastBlobContainerSize + blob.getBytes().length;

        }

        if (currentWriteFileSystem != null) {
            currentWriteFileSystem.close();
        }

    }

    @Override
    public byte[] loadBlobData(Blob blob) throws IOException {
        if (blob == null) {
            return null;
        }
        String path = blob.getPath();
        if (path == null || path.trim().isEmpty()) {
            path = blob.getId();
        }
        scanForBlobContainers();
        if (blobContainerIndex == null) {
            createIndexFromBlobContainers();
        }
        String blobContainerName = blobContainerIndex.get(path);
        if (blobContainerName == null) {
            Logger.getAnonymousLogger().warning("no blob container found for blob " + blob.getPath());
            return null;
        }

        FileSystem fsRead = getReadFileSystem(blobContainerName);

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
        
        // preload blobs
        for (Blob blob : referencedBlobs) {
            blob.getBytes();
        }

        String tmpdir = directory + "/.tmp";
        MultiZipBlobStore newZip = new MultiZipBlobStore(tmpdir);
        newZip.writeBlobs(referencedBlobs);
        newZip.close();
        this.close();
        
        // delete old
        Collection<File> blobContainers = FileUtils.listFiles(new File(directory),
                FileFilterUtils.prefixFileFilter(BLOB_STORE_DEFAULT_FILENAME), null);
        for (File file : blobContainers) {
            file.delete();
        }
        
        // move new
        blobContainers = FileUtils.listFiles(new File(tmpdir),
                FileFilterUtils.prefixFileFilter(BLOB_STORE_DEFAULT_FILENAME), null);
        File destination = new File(directory);
        for (File file : blobContainers) {
            FileUtils.moveToDirectory(file, destination, false);
        }
        
        // delete temp
        FileUtils.deleteDirectory(new File(tmpdir));
        
        blobContainerIndex.clear();
        scanForBlobContainers();
        createIndexFromBlobContainers();
        
    }

    @Override
    public void close() throws IOException {
        for (FileSystem fs : readFileSystems.values()) {
            if (fs != null && fs.isOpen()) {
                fs.close();
            }
        }

    }

    @Override
    public void delete() throws IOException {
        close();
        Collection<File> blobContainers = FileUtils.listFiles(new File(directory),
                FileFilterUtils.prefixFileFilter(BLOB_STORE_DEFAULT_FILENAME), null);
        for (File f : blobContainers) {
            f.delete();
        }
        readFileSystems.clear();
        scanForBlobContainers();
    }
    
    public long numEntries() throws IOException {
        return blobContainerIndex.size();
    }

    private void scanForBlobContainers() throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Collection<File> blobContainers = FileUtils.listFiles(dir,
                FileFilterUtils.prefixFileFilter(BLOB_STORE_DEFAULT_FILENAME), null);
        for (File file : blobContainers) {
            String filenameSuffix = file.getName().substring(file.getName().indexOf('.') + 1);
            try {
                int suffixInt = Integer.parseInt(filenameSuffix);
                if (suffixInt > lastBlobContainerNum) {
                    lastBlobContainer = file;
                    lastBlobContainerNum = suffixInt;
                    lastBlobContainerSize = file.length();
                }
                getReadFileSystem(file.getName());
            } catch (NumberFormatException e) {
                Logger.getAnonymousLogger().log(Level.WARNING,
                        "There was an error parsing the suffix of binary file " + file.getAbsolutePath(), e);
            }
        }
    }

    private void createIndexFromBlobContainers() throws IOException {
        blobContainerIndex = new HashMap<>();
        for (String name : readFileSystems.keySet()) {
            FileSystem fs = readFileSystems.get(name);
            Path path = fs.getPath("/");
            createIndexFromBlobContainer(path, name);
        }
    }

    private void createIndexFromBlobContainer(Path path, String blobContainerName) throws IOException {
        Stream<Path> list = Files.list(path);
        Iterator<Path> iterator = list.iterator();
        while (iterator.hasNext()) {
            Path p = iterator.next();
            if (Files.isDirectory(p)) {
                createIndexFromBlobContainer(p, blobContainerName);
            } else {
                // substring(1) removes first /
                blobContainerIndex.put(p.toString().substring(1), blobContainerName);
            }
        }
        list.close();
    }

    private void createNextBinary() throws IOException {
        lastBlobContainerNum++;
        lastBlobContainer = new File(directory + "/" + BLOB_STORE_DEFAULT_FILENAME + "." + lastBlobContainerNum);
        lastBlobContainerSize = 0;
    }

    private FileSystem getReadFileSystem(String name) throws IOException {
        FileSystem fileSystem = readFileSystems.get(name);
        if (fileSystem == null) {
            Path path = Paths.get(directory + "/" + name);
            URI uri = URI.create("jar:" + path.toUri());
            Map<String, String> env = new HashMap<>();
            fileSystem = FileSystems.newFileSystem(uri, env);
            readFileSystems.put(name, fileSystem);
        }
        return fileSystem;
    }

    private void closeReadFileSystem(String name) throws IOException {
        FileSystem fileSystem = readFileSystems.get(name);
        if (fileSystem != null) {
            fileSystem.close();
            readFileSystems.remove(name);
        }
    }

}