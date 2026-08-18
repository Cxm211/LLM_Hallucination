    public ZipArchiveInputStream(InputStream inputStream,
                                 String encoding,
                                 boolean useUnicodeExtraFields,
                                 boolean allowStoredEntriesWithDataDescriptor) {
        zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        in = new PushbackInputStream(inputStream, buf.capacity());
        this.allowStoredEntriesWithDataDescriptor =
            allowStoredEntriesWithDataDescriptor;
        // haven't read anything so far
    }

// trigger testcase
@Test
    public void testReadingOfFirstStoredEntry() throws Exception {
        ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(getFile("COMPRESS-264.zip")));
        
        try {
            ZipArchiveEntry ze = in.getNextZipEntry();
            assertEquals(5, ze.getSize());
            assertArrayEquals(new byte[] {'d', 'a', 't', 'a', '\n'},
                              IOUtils.toByteArray(in));
        } finally {
            in.close();
        }
    }
