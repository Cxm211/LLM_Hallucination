// org/apache/commons/compress/archivers/zip/Maven221MultiVolumeTest.java
public void testTruncatedStoredEntry() throws IOException {
        // Create a ZIP with a stored entry and truncate it
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        byte[] data = new byte[10];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
        zos.close();
        byte[] fullZip = baos.toByteArray();
        // Truncate after half of the data
        byte[] truncatedZip = Arrays.copyOf(fullZip, fullZip.length / 2);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(truncatedZip));
        ArchiveEntry readEntry = zis.getNextEntry();
        assertNotNull(readEntry);
        byte[] buf = new byte[1024];
        try {
            while (zis.read(buf) != -1) {
                // loop
            }
            fail("Expected IOException for truncated stored entry");
        } catch (IOException e) {
            assertEquals("Truncated ZIP file", e.getMessage());
        }
    }
