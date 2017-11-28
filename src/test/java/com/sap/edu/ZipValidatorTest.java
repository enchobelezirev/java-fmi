package com.sap.edu;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class ZipValidatorTest {

    private ZipValidator validator;

    @Before
    public void setUp() {
        this.validator = createZipValidator();
    }

    @Test
    public void testBasicZipBomb() throws IOException {
        Validation zipValidation = validator.validate(Paths.get("src/test/resources/basic-bomb.zip"));
        assertTrue(zipValidation == Validation.FAILED);
    }

    @Test
    public void testForZipBombWithOneLayer() throws IOException {
        Validation zipValidation = validator.validate(Paths.get("src/test/resources/one-layer-bomb.zip"));
        assertTrue(zipValidation == Validation.FAILED);
    }

    @Test
    public void testForZipBombWithTwoLayers() throws IOException {
        Validation zipValidation = validator.validate(Paths.get("src/test/resources/second-layer-bomb.zip"));
        assertTrue(zipValidation == Validation.FAILED);
    }

    @Test
    public void testFoZipBombWithThreeOrMoreLayers() throws IOException {
        Validation zipValidation = validator.validate(Paths.get("src/test/resources/third-layer-bomb.zip"));
        assertTrue(zipValidation == Validation.FAILED);
    }

    @Test
    public void testNotExistingArchive() throws IOException {
        Validation zipValidation = validator.validate(Paths.get("src/test/resources/not-existing-archive.zip"));
        assertTrue(zipValidation == Validation.FAILED);
    }

    protected ZipValidator createZipValidator() {
        return new ZipValidator();
    }
}
