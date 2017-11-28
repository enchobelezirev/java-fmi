package com.sap.edu;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipBombCreator {

    private static final String DEFAUTLT_ARCHIVE_EXTENSION = ".zip";
    private static final String DEFAULT_FILE_EXTENSION = ".txt";
    private static final byte[] BOMB_DATA = new byte[4096];
    private static final int FILE_DUPLICATES_COUNT = 5;

    public Path createZipBomb(String zipBombName, Path location) {
        Path zipBombFilePath = Paths.get(location.toString(), zipBombName);
        ZipOutputStream zos = null;
        try {
            List<Path> innerZipFilesPaths = prepareZipBombContent(zipBombName);
            zos = new ZipOutputStream(Files.newOutputStream(zipBombFilePath));
            for (Path innerZipFilePath : innerZipFilesPaths) {
                addFileToZip(zos, innerZipFilePath);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                MessageFormat.format("Cannot create file with name {0} in {1}: {2}", zipBombName, location, e.getMessage()));
        } finally {
            closeQuietly(zos);
        }
        return zipBombFilePath;
    }

    private List<Path> prepareZipBombContent(String zipBombName) throws IOException {
        Path bombFile = createBombFile(zipBombName);
        List<Path> bombFiles = multiplyFiles(bombFile, DEFAULT_FILE_EXTENSION);
        Path zipFilePath = createZipFile(bombFiles);
        List<Path> innerZipFilesPaths = multiplyFiles(zipFilePath, DEFAUTLT_ARCHIVE_EXTENSION);
        return innerZipFilesPaths;
    }

    private Path createZipFile(List<Path> files) throws IOException {
        Path zipFileLocation = Files.createTempFile("zip-Archive", DEFAUTLT_ARCHIVE_EXTENSION);
        addFilesToZip(zipFileLocation, files);
        return zipFileLocation;
    }

    private void addFilesToZip(Path zipFile, List<Path> fileEntries) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (Path filePath : fileEntries) {
                addFileToZip(zipOutputStream, filePath);
            }
        }
    }

    private void addFileToZip(ZipOutputStream zipOutputStream, Path fileEntry) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(fileEntry.getFileName().toString()));
        Files.copy(fileEntry, zipOutputStream);
        zipOutputStream.closeEntry();
    }

    private List<Path> multiplyFiles(Path sourceFile, String extension) throws IOException {
        List<Path> duplicateFilePaths = new ArrayList<>();
        for (int i = 0; i < FILE_DUPLICATES_COUNT; i++) {
            Path duplicateFilePath = Files.createTempFile(sourceFile.getFileName().toString(), "bomb-file" + extension);
            duplicateFilePaths.add(duplicateFilePath);
            copyFile(sourceFile, duplicateFilePath);
        }
        return duplicateFilePaths;
    }

    private void copyFile(Path sourceFile, Path target) throws IOException {
        try (OutputStream targetFileOutputStream = Files.newOutputStream(target)) {
            Files.copy(sourceFile, targetFileOutputStream);
        }
    }

    private Path createBombFile(String zipBombName) throws IOException {
        Path bombFilePath = Files.createTempFile(zipBombName, "bomb-file");
        try (OutputStream bombFileOutputStream = Files.newOutputStream(bombFilePath)) {
            bombFileOutputStream.write(BOMB_DATA);
        }
        return bombFilePath;
    }

    private void closeQuietly(OutputStream closable) {
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    public static void main(String[] args) {
        System.out.println(new ZipBombCreator().createZipBomb("test-zip-bomb.zip", Paths.get("/Users/i321665/Desktop")));
    }
}
