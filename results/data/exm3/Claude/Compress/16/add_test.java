// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java
@Test
public void testValidTarArchiveWithNullEntry() throws Exception {
    InputStream is = null;
    try {
        // Create a valid TAR header that returns null from getNextEntry()
        byte[] tarHeader = new byte[512];
        // Set name to empty string (null terminated)
        tarHeader[0] = 0;
        // Set mode (octal 000644)
        System.arraycopy("000644 \0".getBytes(), 0, tarHeader, 100, 8);
        // Set uid (octal 000000)
        System.arraycopy("000000 \0".getBytes(), 0, tarHeader, 108, 8);
        // Set gid (octal 000000)
        System.arraycopy("000000 \0".getBytes(), 0, tarHeader, 116, 8);
        // Set size (octal 00000000000)
        System.arraycopy("00000000000 ".getBytes(), 0, tarHeader, 124, 12);
        // Set mtime (octal 00000000000)
        System.arraycopy("00000000000 ".getBytes(), 0, tarHeader, 136, 12);
        // Set checksum to spaces initially
        System.arraycopy("        ".getBytes(), 0, tarHeader, 148, 8);
        // Set typeflag to '0' (regular file)
        tarHeader[156] = '0';
        // Calculate checksum
        long checksum = 0;
        for (int i = 0; i < tarHeader.length; i++) {
            checksum += tarHeader[i] & 0xff;
        }
        String checksumStr = String.format("%06o\0 ", checksum);
        System.arraycopy(checksumStr.getBytes(), 0, tarHeader, 148, 8);
        
        is = new BufferedInputStream(new ByteArrayInputStream(tarHeader));
        try {
            new ArchiveStreamFactory().createArchiveInputStream(is);
            fail("created an input stream for an invalid tar archive");
        } catch (ArchiveException ae) {
            assertTrue(ae.getMessage().startsWith("No Archiver found"));
        }
    } finally {
        if (is != null) {
            is.close();
        }
    }
}