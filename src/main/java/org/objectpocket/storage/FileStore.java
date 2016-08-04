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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectpocket.Blob;
import org.objectpocket.storage.blob.BlobStore;
import org.objectpocket.util.JsonHelper;

import com.google.gson.Gson;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class FileStore implements ObjectStore {

    protected String directory;

    protected final String INDEX_FILE_NAME = ".op_index";
    protected ObjectPocketIndex index = new ObjectPocketIndex();
    protected ObjectPocketIndex indexBackup = new ObjectPocketIndex();

    private BlobStore blobStore;

    public FileStore(String directory) {
        this.directory = directory;
    }

    @Override
    public boolean exists() {
        File indexFile = new File(directory + "/" + INDEX_FILE_NAME);
        if (indexFile.exists()) {
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getAvailableObjectTypes() throws IOException {
        readIndexFile();
        return index.getTypeToFilenamesMapping().keySet();
    }

    @Override
    public Map<String, String> readJsonObjects(String typeName) throws IOException {
        if (typeName == null) {
            return null;
        }
        Set<String> filenames = index.getTypeToFilenamesMapping().get(typeName);
        Map<String, String> objects = new HashMap<String, String>();
        if (filenames != null) {
            for (String filename : filenames) {

                // maximum fast file reading
                StringBuilder stringBuilder = new StringBuilder();
                try (BufferedReader br = getBufferedReader(filename)) {
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }

                String s = null;
                // remove first occurrence of "[", as this is the start of the
                // container array
                // all other object splitting will work with that!
                int index = stringBuilder.indexOf("[");
                if (index > -1) {
                    s = stringBuilder.substring(index + 1, stringBuilder.length());
                } else {
                    throw new IOException("The file " + directory + "/" + filename + " does not contain valid JSON. "
                            + getReadErrorMessage());
                }
                List<String> jsonStrings = JsonHelper.splitToTopLevelJsonObjects(s);
                for (int i = 0; i < jsonStrings.size(); i++) {
                    String[] typeAndIdFromJson = JsonHelper.getTypeAndIdFromJson(jsonStrings.get(i));
                    if (typeAndIdFromJson[0].equals(typeName)) {
                        objects.put(jsonStrings.get(i), typeAndIdFromJson[1]);
                    }
                }
            }
        } else {
            Logger.getAnonymousLogger().log(Level.WARNING,
                    "File for requested type: " + typeName + " does not exist in data store.");
        }
        return objects;
    }

    @Override
    public void writeJsonObjects(Map<String, Map<String, Set<String>>> jsonObjects) throws IOException {
        // TODO: delete file when receiving empty list!!
        // 1. possibility:
        // delet all files everytime before writing
        // 2. possibility:
        // delete file by file when necessary (better when using zip archive)
        backupCurrentIndex();
        index = new ObjectPocketIndex();
        for (String typeName : jsonObjects.keySet()) {
            Map<String, Set<String>> objectsForType = jsonObjects.get(typeName);
            for (String filename : objectsForType.keySet()) {
                String filenameOnDisc = filename + ".json";
                OutputStreamWriter out = getOutputStreamWriter(filenameOnDisc);
                addToIndex(typeName, filenameOnDisc);
                out.write(JsonHelper.JSON_PREFIX + "\n");
                Set<String> objectSet = objectsForType.get(filename);
                Iterator<String> iterator = objectSet.iterator();
                while (iterator.hasNext()) {
                    out.write(iterator.next());
                    if (iterator.hasNext()) {
                        out.write(",");
                    }
                    out.write("\n");
                }
                out.write(JsonHelper.JSON_SUFFIX);
                out.flush();
                closeOutputStreamWriter(out);
            }
        }
        removeUnusedFiles();
        writeIndexFile();
        finishWrite();
    }

    public void setBlobStore(BlobStore blobStore) {
        this.blobStore = blobStore;
    }

    @Override
    public void writeBlobs(Set<Blob> blobs) throws IOException {
        this.blobStore.writeBlobs(blobs);
    }

    @Override
    public byte[] loadBlobData(Blob blob) throws IOException {
        return this.blobStore.loadBlobData(blob);
    }

    @Override
    public void cleanup(Set<Blob> referencedBlobs) throws IOException {
        this.blobStore.cleanup(referencedBlobs);
    }

    @Override
    public void close() throws IOException {
        this.blobStore.close();
    }

    @Override
    public void delete() throws IOException {
        this.blobStore.delete();
    }

    @Override
    public String getSource() {
        return directory;
    }

    protected OutputStreamWriter getOutputStreamWriter(String filename) throws IOException {
        File file = initFile(filename, true, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        return outputStreamWriter;
    }

    protected void closeOutputStreamWriter(OutputStreamWriter out) throws IOException {
        out.close();
    }

    protected BufferedReader getBufferedReader(String filename) throws IOException {
        File file = initFile(filename, true, false);
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }

    protected void finishWrite() throws IOException {

    }

    protected String getReadErrorMessage() {
        return "";
    }

    protected File initFile(String typeName, boolean read, boolean write) throws IOException {
        File dir = initFileStore(read, write);
        String filename = dir.getPath() + "/" + typeName;
        File f = new File(filename);
        if (write && !f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new IOException("File could not be created. " + filename, e);
            }
        }
        if (!f.exists()) {
            throw new IOException("File does not exist. " + filename);
        }
        if (read && !f.canRead()) {
            throw new IOException("File is not readable. " + filename);
        }
        if (write && !f.canWrite()) {
            throw new IOException("File is not writeable. " + filename);
        }
        return f;
    }

    private File initFileStore(boolean read, boolean write) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            if (write) {
                dir.mkdirs();
            } else {
                throw new IOException("Store does not exist. Nothing to load here.");
            }
        }
        if (!dir.exists()) {
            throw new IOException("File store does not exist. " + directory);
        }
        if (!dir.isDirectory()) {
            throw new IOException("File store is not a directory. " + directory);
        }
        return dir;
    }

    private void addToIndex(String typeName, String filename) {
        if (index.getTypeToFilenamesMapping().get(typeName) == null) {
            index.getTypeToFilenamesMapping().put(typeName, new HashSet<String>());
        }
        index.getTypeToFilenamesMapping().get(typeName).add(filename);
    }

    protected void readIndexFile() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = getBufferedReader(INDEX_FILE_NAME)) {
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new IOException("Could not read index file. " + directory + "/" + INDEX_FILE_NAME + ". "
                    + getReadErrorMessage(), e);
        }
        if (sb.length() > 0) {
            Gson gson = new Gson();
            ObjectPocketIndex o = gson.fromJson(sb.toString(), ObjectPocketIndex.class);
            if (o != null) {
                index = o;
                return;
            }
        }
        throw new IOException("Could not parse index file data to index object. " + directory + "/" + INDEX_FILE_NAME
                + ". " + getReadErrorMessage());
    }

    protected void writeIndexFile() throws IOException {
        try {
            writeIndexFileData(getOutputStreamWriter(INDEX_FILE_NAME));
        } catch (IOException e) {
            throw new IOException("Could not write index file. " + INDEX_FILE_NAME, e);
        }
    }

    protected void writeIndexFileData(OutputStreamWriter out) throws IOException {
        Gson gson = new Gson();
        String jsonString = gson.toJson(index);
        out.write(jsonString);
        out.flush();
        closeOutputStreamWriter(out);
    }

    protected void backupCurrentIndex() {
        indexBackup = index.clone();
    }

    protected void removeUnusedFiles() {
        if (indexBackup == null || index == null) {
            return;
        }
        Set<String> filesToRemove = new HashSet<String>();
        Map<String, Set<String>> oldMapping = indexBackup.getTypeToFilenamesMapping();
        Map<String, Set<String>> newMapping = index.getTypeToFilenamesMapping();
        // add all old filenames
        for (String typeName : oldMapping.keySet()) {
            filesToRemove.addAll(oldMapping.get(typeName));
        }
        // remove all that are still in use
        for (String typeName : newMapping.keySet()) {
            filesToRemove.removeAll(newMapping.get(typeName));
        }
        // remove files
        for (String filename : filesToRemove) {
            File f = new File(directory + "/" + filename);
            if (!f.delete()) {
                Logger.getAnonymousLogger().severe("Could not remove file from store. " + f.getPath());
            }
        }
    }

}
