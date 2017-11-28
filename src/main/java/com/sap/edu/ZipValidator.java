package com.sap.edu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipValidator {
    private static final long MAX_ENTRY_SIZE = 4 * 1024 * 1024L;

    public Validation validate(Path zipArchive) throws IOException {
        return validateZipArchive(zipArchive);
    }

    private Validation validateZipArchive(Path zipArchive) throws IOException {
        if (!Files.exists(zipArchive)) {
            return Validation.FAILED;
        }
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipArchive))) {
            return validate(zis);
        }
    }

    private Validation validate(ZipInputStream zis) throws IOException {
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            Validation nestedZipValidation = checkForNestedZipArchive(zis);
            if (nestedZipValidation == Validation.FAILED) {
                return Validation.FAILED;
            }
            Validation entryValidation = validateEntrySize(entry);
            if (entryValidation == Validation.FAILED) {
                return Validation.FAILED;
            }
        }
        return Validation.SUCCESS;
    }

    private Validation checkForNestedZipArchive(ZipInputStream zis) throws IOException {
        try (ZipInputStream nestedZip = new ZipInputStream(zis)) {
            ZipEntry nestedZipEntry = null;
            if ((nestedZipEntry = nestedZip.getNextEntry()) != null) {
                return validateNestedZip(nestedZip, nestedZipEntry);
            }
        }
        return Validation.SUCCESS;
    }

    private Validation validateNestedZip(ZipInputStream nestedZip, ZipEntry nestedZipEntry) throws IOException {
        Validation validationState = validateEntrySize(nestedZipEntry);
        if (validationState == Validation.FAILED) {
            return validationState;
        }
        return validate(nestedZip);
    }

    private Validation validateEntrySize(ZipEntry entry) {
        if (isTooLarge(entry)) {
            System.out.println(MessageFormat.format("File name {0}, file size {1}", entry.getName(), entry.getSize()));
        }
        return isTooLarge(entry) ? Validation.FAILED : Validation.SUCCESS;
    }

    private boolean isTooLarge(ZipEntry entry) {
        return entry.getSize() > MAX_ENTRY_SIZE;
    }
}
