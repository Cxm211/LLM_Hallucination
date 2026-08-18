// org/apache/commons/compress/archivers/zip/DataDescriptorTest.java
@Test
    public void doesntWriteDataDescriptorForStoredRawEntries() throws IOException {
        ByteArrayOutputStream init = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(init)) {
            ZipArchiveEntry ze = new ZipArchiveEntry("test1.txt");
            ze.setMethod(ZipArchiveOutputStream.STORED);
            byte[] data = "foo".getBytes("UTF-8");
            ze.setSize(data.length);
            ze.setCompressedSize(data.length);
            ze.setCrc(new CRC32());
            ((CRC32)ze.getCrc()).update(data);
            zos.putArchiveEntry(ze);
            zos.write(data);
            zos.closeArchiveEntry();
        }

        File f = new File(dir, "test_stored.zip");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(init.toByteArray());
        }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ZipArchiveEntry zae;
        try (ZipFile zf = new ZipFile(f);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(o)) {
            zae = zf.getEntry("test1.txt");
            zos.addRawArchiveEntry(zae, zf.getRawInputStream(zae));
        }

        byte[] data = o.toByteArray();
        byte[] gpbInLFH = Arrays.copyOfRange(data, 6, 8);
        assertArrayEquals(new byte[] { 0, 8 }, gpbInLFH);

        int cdhStart = findCentralDirectory(data);
        byte[] gpbInCDH = Arrays.copyOfRange(data, cdhStart + 8, cdhStart + 10);
        assertArrayEquals(new byte[] { 0, 8 }, gpbInCDH);

        int ddStart = cdhStart - 16;
        assertNotEquals(ZipLong.DD_SIG, new ZipLong(data, ddStart));
        long crcFromLFH = ZipLong.getValue(data, 14);
        long sizeFromLFH = ZipLong.getValue(data, 22);
        assertEquals(3, sizeFromLFH);

        long crcFromCDH = ZipLong.getValue(data, cdhStart + 16);
        assertEquals(crcFromLFH, crcFromCDH);
    }