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

package org.objectpocket.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class ZipFileStore extends FileStore {

    private String zipfile;
    private ZipOutputStream zipOutputStream;
    private OutputStreamWriter outputStreamWriter;
    private int compressionLevel = 0;

    public ZipFileStore(String filename, int compressionLevel) {
	super(null);
	this.compressionLevel = compressionLevel;
	this.zipfile = filename;
    }

    @Override
    public Set<String> getAvailableObjectTypes() throws IOException {
	readIndexFile();
	return objectPocketIndex.getTypeToFilenamesMapping().keySet();
    }

    @Override
    public String getSource() {
	return this.zipfile;
    }

    protected void finishWrite() throws IOException {
	zipOutputStream.closeEntry();
	zipOutputStream.close();
	zipOutputStream = null;
	outputStreamWriter = null;
    }

    protected OutputStreamWriter getOutputStreamWriter(String filename)
	    throws IOException {
	if (zipOutputStream == null) {
	    initZipFile(true, true);
	    OutputStream out = new FileOutputStream(zipfile);
	    zipOutputStream = new ZipOutputStream(out);
	    zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
	    zipOutputStream.setLevel(this.compressionLevel);
	    outputStreamWriter = new OutputStreamWriter(zipOutputStream);
	} else {
	    zipOutputStream.closeEntry();
	}
	ZipEntry entry = new ZipEntry(filename);
	zipOutputStream.putNextEntry(entry);
	return outputStreamWriter;
    }

    protected BufferedReader getBufferedReader(String filename)
	    throws IOException {
	InputStream in = new FileInputStream(zipfile);
	ZipInputStream zipInputStream = new ZipInputStream(in);
	ZipEntry nextEntry = null;
	while ((nextEntry = zipInputStream.getNextEntry()) != null) {
	    if (nextEntry.getName().equals(filename)) {
		return new BufferedReader(new InputStreamReader(zipInputStream));
	    }
	}
	throw new IOException("Could not read file " + filename + " in zip "
		+ zipfile);
    }

    private void initZipFile(boolean read, boolean write) throws IOException {
	File f = new File(this.zipfile);
	if (!f.exists()) {
	    if (write) {
		f.getParentFile().mkdirs();
	    } else {
		throw new IOException(
			"Store does not exist. Nothing to load here.");
	    }
	}
	if (!f.getParentFile().exists()) {
	    throw new IOException(
		    "Directory does not exist and could not be created. "
			    + f.getParent());
	}
	if (!f.exists() && !f.createNewFile()) {
	    throw new IOException("Could not create zip file. " + f.getPath());
	}
	this.directory = f.getParent();
	boolean indexFileFound = false;
	InputStream in = new FileInputStream(zipfile);
	try (ZipInputStream zipIn = new ZipInputStream(in)) {
	    ZipEntry entry = null;
	    while ((entry = zipIn.getNextEntry()) != null) {
		if (entry.getName().equals(INDEX_FILE_NAME)) {
		    indexFileFound = true;
		}
	    }
	} catch (IOException e) {
	    throw new IOException("Could not read index file from "
		    + this.zipfile, e);
	}
	if (!indexFileFound) {
	    Logger.getAnonymousLogger().warning(
		    "Could not find index file. Will create.");
	    writeIndexFileData(getOutputStreamWriter(INDEX_FILE_NAME));
	    finishWrite();
	}
    }

    protected void readIndexFile() throws IOException {
	initZipFile(true, false);
	String line = null;
	InputStream in = new FileInputStream(zipfile);
	try (ZipInputStream zipIn = new ZipInputStream(in)) {
	    ZipEntry entry = null;
	    while ((entry = zipIn.getNextEntry()) != null) {
		if (entry.getName().equals(INDEX_FILE_NAME)) {
		    line = readDataFromZip(zipIn);
		}
	    }
	} catch (IOException e) {
	    throw new IOException("Could not read index file from "
		    + this.zipfile, e);
	}
	if (line != null) {
	    Gson gson = new Gson();
	    ObjectPocketIndex o = gson.fromJson(line, ObjectPocketIndex.class);
	    if (o != null) {
		objectPocketIndex = o;
		return;
	    }
	}
	throw new IOException(
		"Could not parse index file data to index object. "
			+ this.zipfile);
    }

    private String readDataFromZip(ZipInputStream zipIn) throws IOException {
	byte[] buf = new byte[1024];
	StringBuilder sb = new StringBuilder();
	int length = -1;
	while ((length = zipIn.read(buf)) != -1) {
	    sb.append(new String(buf, 0, length));
	}
	return sb.toString();
    }

}
