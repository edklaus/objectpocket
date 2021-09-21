/*
 * Copyright (C) 2021 Edmund Klaus
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class FileStoreBackup {

    private FileStore fileStore;
    protected static final String BACKUP_DIR_NAME = "_backups";
    protected static final String TEMP_BACKUP_DIR_NAME = "tmp";
    protected static final String OLD_BACKUP_DIR_NAME = ".bak";
    private int maxBackupSizeM = 250; // MBs

    private File tempBackupDir;
    private File backupDir;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public FileStoreBackup(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    protected void createBackup() throws IOException {
        Set<File> filesToBackup = collectFilesForBackup();
        if (filesToBackup != null && !filesToBackup.isEmpty()) {
            initBackupDir();
            initTempBackupDir();
            // do this synchronous, to ensure that all files are backed up
            // before new data is written to the files
            final Set<File> filesToWrite = copyBackupFilesToTemp(filesToBackup);
            // do this asynchronous, since it's the most time consuming part
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        File zipBackup = writeBackupZip(filesToWrite);
                        Logger.getAnonymousLogger().info("Wrote backup to " + zipBackup.getAbsolutePath());
                        clearTempBackupDir();
                        cleanupBackups();
                        removeOldBackupDir();
                    } catch (IOException e) {
                        Logger.getAnonymousLogger().log(Level.SEVERE, "Coul dnot create backup.", e);
                    }
                }
            };
            new Thread(r).start();
        }
    }

    private Set<File> collectFilesForBackup() {
        File storeDir = new File(fileStore.getSource());
        if (storeDir.exists() && storeDir.isDirectory()) {
            Set<File> filesToBackup = new HashSet<>();
            File[] files = storeDir.listFiles();
            for (File file : files) {
                String name = file.getName();
                if (!file.isDirectory()
                        && (name.endsWith(fileStore.FILENAME_SUFFIX) || name.equals(fileStore.INDEX_FILE_NAME))) {
                    filesToBackup.add(file);
                }
            }
            return filesToBackup;
        }
        return null;
    }

    private void initBackupDir() {
        File storeDir = new File(fileStore.getSource());
        if (storeDir.exists() && storeDir.isDirectory()) {
            backupDir = new File(storeDir.getAbsolutePath() + "/" + BACKUP_DIR_NAME);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
        }
    }

    private void initTempBackupDir() {
        tempBackupDir = new File(backupDir.getAbsolutePath() + "/" + TEMP_BACKUP_DIR_NAME);
        if (!tempBackupDir.exists()) {
            tempBackupDir.mkdirs();
        }
    }

    private void clearTempBackupDir() throws IOException {
        if (tempBackupDir != null && tempBackupDir.exists()) {
            FileUtils.cleanDirectory(tempBackupDir);
        }
    }

    private Set<File> copyBackupFilesToTemp(Set<File> filesToMove) throws IOException {
        FileUtils.copyToDirectory(filesToMove, tempBackupDir);
        Set<File> copiedFiles = Arrays.stream(tempBackupDir.listFiles()).collect(Collectors.toSet());
        return copiedFiles;
    }

    private File writeBackupZip(Set<File> filesToArchive) throws IOException {

        File zipFile = new File(backupDir.getAbsolutePath() + "/" + formatter.format(new Date()) + ".zip");
        zipFile.createNewFile();

        try (ArchiveOutputStream out = new ZipArchiveOutputStream(zipFile)) {
            for (File f : filesToArchive) {
                if (f.isFile()) {
                    ArchiveEntry entry = out.createArchiveEntry(f, f.getName());
                    out.putArchiveEntry(entry);
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, out);
                    }
                }
                out.closeArchiveEntry();
            }
            out.finish();
        }

        return zipFile;

    }

    /**
     * cleanup the files in backup dir to keep a constant disk mem usage
     */
    private void cleanupBackups() {
        long backupDirSizeM = FileUtils.sizeOfDirectory(backupDir) / 1024_000;
        if (backupDirSizeM > maxBackupSizeM) {
            File[] files = backupDir.listFiles();
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            files[0].delete();
        }
    }
    
    private void removeOldBackupDir() {
        File storeDir = new File(fileStore.getSource());
        if (storeDir.exists() && storeDir.isDirectory()) {
            File oldBackupDir = new File(storeDir.getAbsolutePath() + "/" + OLD_BACKUP_DIR_NAME);
            if (oldBackupDir.exists()) {
                FileUtils.deleteQuietly(oldBackupDir);
            }
        }
    }

    public void setMaxBackupSizeM(int maxBackupSizeM) {
        this.maxBackupSizeM = maxBackupSizeM;
    }

    public int getMaxBackupSizeM() {
        return maxBackupSizeM;
    }

}
