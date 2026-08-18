// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
    public void testThrowOnInvalidSignatureAfterValidEntry() throws Exception {
        // Create a ZIP with one valid stored entry and then an invalid signature
        byte[] zipData = new byte[] {
            // Local file header signature
            0x50, 0x4b, 0x03, 0x04,
            // Version needed to extract (10)
            0x0a, 0x00,
            // General purpose bit flag (0)
            0x00, 0x00,
            // Compression method (STORED)
            0x00, 0x00,
            // Last mod file time
            0x00, 0x00,
            // Last mod file date
            0x00, 0x00,
            // CRC-32 of "hello" (0x3610a686 little-endian)
            (byte)0x86, (byte)0xa6, 0x10, 0x36,
            // Compressed size (5)
            0x05, 0x00, 0x00, 0x00,
            // Uncompressed size (5)
            0x05, 0x00, 0x00, 0x00,
            // File name length (8)
            0x08, 0x00,
            // Extra field length (0)
            0x00, 0x00,
            // File name "test.txt"
            0x74, 0x65, 0x73, 0x74, 0x2e, 0x74, 0x78, 0x74,
            // File data "hello"
            0x68, 0x65, 0x6c, 0x6c, 0x6f,
            // Invalid signature
            0x12, 0x34, 0x56, 0x78
        };
        InputStream is = new ByteArrayInputStream(zipData);
        ZipArchiveInputStream zip = new ZipArchiveInputStream(is);
        ZipArchiveEntry entry = zip.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        // Now the next entry should throw due to invalid signature
        try {
            zip.getNextZipEntry();
            fail("ZipException expected");
        } catch (ZipException expected) {
            assertTrue(expected.getMessage().contains("Unexpected record signature"));
        } finally {
            zip.close();
        }
    }
