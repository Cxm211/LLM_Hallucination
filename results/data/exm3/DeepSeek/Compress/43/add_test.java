// org/apache/commons/compress/archivers/zip/DataDescriptorTest.java
@Test
    public void doesntWriteDataDescriptorWhenAddingRawEntriesWithZip64() throws IOException {
        ByteArrayOutputStream init = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(init)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("large.txt");
            entry.setSize(ZipConstants.ZIP64_MAGIC + 1); // Force Zip64 extra
            zos.putArchiveEntry(entry);
            byte[] data = new byte[1024];
            Arrays.fill(data, (byte) 'x');
            zos.write(data);
            zos.closeArchiveEntry();
        }

        File f = new File(dir, "test_zip64.zip");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(init.toByteArray());
        }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ZipArchiveEntry zae;
        try (ZipFile zf = new ZipFile(f);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(o)) {
            zae = zf.getEntry("large.txt");
            zos.addRawArchiveEntry(zae, zf.getRawInputStream(zae));
        }

        byte[] data = o.toByteArray();
        byte[] gpbInLFH = Arrays.copyOfRange(data, 6, 8);
        assertArrayEquals(new byte[] { 0, 8 }, gpbInLFH); // No data descriptor flag, only EFS

        int cdhStart = findCentralDirectory(data);
        byte[] gpbInCDH = Arrays.copyOfRange(data, cdhStart + 8, cdhStart + 10);
        assertArrayEquals(new byte[] { 0, 8 }, gpbInCDH);

        int ddStart = cdhStart - 16;
        assertNotEquals(ZipLong.DD_SIG, new ZipLong(data, ddStart)); // No data descriptor
    }
