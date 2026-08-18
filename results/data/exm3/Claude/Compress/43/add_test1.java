// org/apache/commons/compress/archivers/zip/DataDescriptorTest.java
@Test
    public void writesDataDescriptorForNonRawDeflatedEntries() throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(o)) {
            ZipArchiveEntry ze = new ZipArchiveEntry("test1.txt");
            ze.setMethod(ZipArchiveOutputStream.DEFLATED);
            zos.putArchiveEntry(ze);
            zos.write("foo".getBytes("UTF-8"));
            zos.closeArchiveEntry();
        }

        byte[] data = o.toByteArray();
        byte[] gpbInLFH = Arrays.copyOfRange(data, 6, 8);
        assertArrayEquals(new byte[] { 8, 8 }, gpbInLFH);

        int cdhStart = findCentralDirectory(data);
        byte[] gpbInCDH = Arrays.copyOfRange(data, cdhStart + 8, cdhStart + 10);
        assertArrayEquals(new byte[] { 8, 8 }, gpbInCDH);

        int lfhSize = 30 + "test1.txt".length();
        long compressedSize = ZipLong.getValue(data, cdhStart + 20);
        int ddStart = lfhSize + (int)compressedSize;
        assertEquals(ZipLong.DD_SIG, new ZipLong(data, ddStart));
    }