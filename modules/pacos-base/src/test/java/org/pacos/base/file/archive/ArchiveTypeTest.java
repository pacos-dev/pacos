package org.pacos.base.file.archive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchiveTypeTest {

    @Test
    void whenGetExtensionCalledThenReturnsCorrectExtension() {
        assertEquals(".zip", ArchiveType.ZIP.getExtension());
        assertEquals(".tar.gz", ArchiveType.TAR_GZ.getExtension());
        assertEquals(".gz", ArchiveType.GZIP.getExtension());
    }

    @Test
    void whenIsZipCalledThenReturnsTrueForZip() {
        assertTrue(ArchiveType.ZIP.isZip());
        assertFalse(ArchiveType.TAR_GZ.isZip());
        assertFalse(ArchiveType.GZIP.isZip());
    }

    @Test
    void whenIsTarGzCalledThenReturnsTrueForTarGz() {
        assertTrue(ArchiveType.TAR_GZ.isTarGz());
        assertFalse(ArchiveType.ZIP.isTarGz());
        assertFalse(ArchiveType.GZIP.isTarGz());
    }
}