// buggy code
    public int read(final byte[] dest, final int offs, final int len)
        throws IOException {
        if (offs < 0) {
            throw new IndexOutOfBoundsException("offs(" + offs + ") < 0.");
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("len(" + len + ") < 0.");
        }
        if (offs + len > dest.length) {
            throw new IndexOutOfBoundsException("offs(" + offs + ") + len("
                                                + len + ") > dest.length(" + dest.length + ").");
        }
        if (this.in == null) {
            throw new IOException("stream closed");
        }

        final int hi = offs + len;
        int destOffs = offs;
        int b;
        while (destOffs < hi && ((b = read0()) >= 0)) {
            dest[destOffs++] = (byte) b;
            count(1);
        }

        int c = (destOffs == offs) ? -1 : (destOffs - offs);
        return c;
    }

// relevant test
// org.apache.commons.compress.ChainingTestCase::testTarGzip
    public void testTarGzip() throws Exception {
        File file = getFile("bla.tgz");
        final TarArchiveInputStream is = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
        final TarArchiveEntry entry = (TarArchiveEntry)is.getNextEntry();
        assertNotNull(entry);
        assertEquals("test1.xml", entry.getName());
        is.close();
    }

// org.apache.commons.compress.ChainingTestCase::testTarBzip2
    public void testTarBzip2() throws Exception {
        File file = getFile("bla.tar.bz2");
        final TarArchiveInputStream is = new TarArchiveInputStream(new BZip2CompressorInputStream(new FileInputStream(file)));
        final TarArchiveEntry entry = (TarArchiveEntry)is.getNextEntry();
        assertNotNull(entry);
        assertEquals("test1.xml", entry.getName());
        is.close();
    }

// org.apache.commons.compress.archivers.SevenZTestCase::testSevenZArchiveCreationUsingCopy
    public void testSevenZArchiveCreationUsingCopy() throws Exception {
        testSevenZArchiveCreation(SevenZMethod.COPY);
    }

// org.apache.commons.compress.archivers.SevenZTestCase::testSevenZArchiveCreationUsingLZMA2
    public void testSevenZArchiveCreationUsingLZMA2() throws Exception {
        testSevenZArchiveCreation(SevenZMethod.LZMA2);
    }

// org.apache.commons.compress.archivers.SevenZTestCase::testSevenZArchiveCreationUsingBZIP2
    public void testSevenZArchiveCreationUsingBZIP2() throws Exception {
        testSevenZArchiveCreation(SevenZMethod.BZIP2);
    }

// org.apache.commons.compress.archivers.SevenZTestCase::testSevenZArchiveCreationUsingDeflate
    public void testSevenZArchiveCreationUsingDeflate() throws Exception {
        testSevenZArchiveCreation(SevenZMethod.DEFLATE);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testAllEmptyFilesArchive
    public void testAllEmptyFilesArchive() throws Exception {
        SevenZFile archive = new SevenZFile(getFile("7z-empty-mhc-off.7z"));
        try {
            assertNotNull(archive.getNextEntry());
        } finally {
            archive.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testHelloWorldHeaderCompressionOffCopy
    public void testHelloWorldHeaderCompressionOffCopy() throws Exception {
        checkHelloWorld("7z-hello-mhc-off-copy.7z");
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testHelloWorldHeaderCompressionOffLZMA2
    public void testHelloWorldHeaderCompressionOffLZMA2() throws Exception {
        checkHelloWorld("7z-hello-mhc-off-lzma2.7z");
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::test7zUnarchive
    public void test7zUnarchive() throws Exception {
        test7zUnarchive(getFile("bla.7z"), SevenZMethod.LZMA);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::test7zDeflateUnarchive
    public void test7zDeflateUnarchive() throws Exception {
        test7zUnarchive(getFile("bla.deflate.7z"), SevenZMethod.DEFLATE);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::test7zDecryptUnarchive
    public void test7zDecryptUnarchive() throws Exception {
        if (isStrongCryptoAvailable()) {
            test7zUnarchive(getFile("bla.encrypted.7z"), SevenZMethod.LZMA, 
                            "foo".getBytes("UTF-16LE"));
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testEncryptedArchiveRequiresPassword
    public void testEncryptedArchiveRequiresPassword() throws Exception {
        try {
            new SevenZFile(getFile("bla.encrypted.7z"));
            fail("shouldn't decrypt without a password");
        } catch (PasswordRequiredException ex) {
            String msg = ex.getMessage();
            assertTrue("Should start with whining about being unable to decrypt",
                       msg.startsWith("Cannot read encrypted content from "));
            assertTrue("Should finish the sentence properly",
                       msg.endsWith(" without a password."));
            assertTrue("Should contain archive's name",
                       msg.contains("bla.encrypted.7z"));
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testCompressedHeaderWithNonDefaultDictionarySize
    public void testCompressedHeaderWithNonDefaultDictionarySize() throws Exception {
        SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-256.7z"));
        try {
            int count = 0;
            while (sevenZFile.getNextEntry() != null) {
                count++;
            }
            assertEquals(446, count);
        } finally {
            sevenZFile.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testSignatureCheck
    public void testSignatureCheck() {
        assertTrue(SevenZFile.matches(SevenZFile.sevenZSignature,
                                      SevenZFile.sevenZSignature.length));
        assertTrue(SevenZFile.matches(SevenZFile.sevenZSignature,
                                      SevenZFile.sevenZSignature.length + 1));
        assertFalse(SevenZFile.matches(SevenZFile.sevenZSignature,
                                      SevenZFile.sevenZSignature.length - 1));
        assertFalse(SevenZFile.matches(new byte[] { 1, 2, 3, 4, 5, 6 }, 6));
        assertTrue(SevenZFile.matches(new byte[] { '7', 'z', (byte) 0xBC,
                                                   (byte) 0xAF, 0x27, 0x1C}, 6));
        assertFalse(SevenZFile.matches(new byte[] { '7', 'z', (byte) 0xBC,
                                                    (byte) 0xAF, 0x27, 0x1D}, 6));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testReadingBackLZMA2DictSize
    public void testReadingBackLZMA2DictSize() throws Exception {
        File output = new File(dir, "lzma2-dictsize.7z");
        SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            outArchive.setContentMethods(Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.LZMA2, 1 << 20)));
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo.txt");
            outArchive.putArchiveEntry(entry);
            outArchive.write(new byte[] { 'A' });
            outArchive.closeArchiveEntry();
        } finally {
            outArchive.close();
        }

        SevenZFile archive = new SevenZFile(output);
        try {
            SevenZArchiveEntry entry = archive.getNextEntry();
            SevenZMethodConfiguration m = entry.getContentMethods().iterator().next();
            assertEquals(SevenZMethod.LZMA2, m.getMethod());
            assertEquals(1 << 20, m.getOptions());
        } finally {
            archive.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testReadingBackDeltaDistance
    public void testReadingBackDeltaDistance() throws Exception {
        File output = new File(dir, "delta-distance.7z");
        SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            outArchive.setContentMethods(Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.DELTA_FILTER, 32),
                                                       new SevenZMethodConfiguration(SevenZMethod.LZMA2)));
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo.txt");
            outArchive.putArchiveEntry(entry);
            outArchive.write(new byte[] { 'A' });
            outArchive.closeArchiveEntry();
        } finally {
            outArchive.close();
        }

        SevenZFile archive = new SevenZFile(output);
        try {
            SevenZArchiveEntry entry = archive.getNextEntry();
            SevenZMethodConfiguration m = entry.getContentMethods().iterator().next();
            assertEquals(SevenZMethod.DELTA_FILTER, m.getMethod());
            assertEquals(32, m.getOptions());
        } finally {
            archive.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZMethodConfigurationTest::shouldAllowNullOptions
    public void shouldAllowNullOptions() {
        Assert.assertNull(new SevenZMethodConfiguration(SevenZMethod.LZMA2, null)
                          .getOptions());
    }

// org.apache.commons.compress.archivers.sevenz.SevenZMethodConfigurationTest::shouldAllowLZMA2OptionsForLZMA2
    public void shouldAllowLZMA2OptionsForLZMA2() {
        Assert.assertNotNull(new SevenZMethodConfiguration(SevenZMethod.LZMA2,
                                                           new LZMA2Options())
                             .getOptions());
    }

// org.apache.commons.compress.archivers.sevenz.SevenZMethodConfigurationTest::shouldAllowNumberForLZMA2
    public void shouldAllowNumberForLZMA2() {
        Assert.assertNotNull(new SevenZMethodConfiguration(SevenZMethod.LZMA2, 42)
                             .getOptions());
    }

// org.apache.commons.compress.archivers.sevenz.SevenZMethodConfigurationTest::shouldAllowNumberForBzip2
    public void shouldAllowNumberForBzip2() {
        Assert.assertNotNull(new SevenZMethodConfiguration(SevenZMethod.BZIP2, 42)
                             .getOptions());
    }

// org.apache.commons.compress.archivers.sevenz.SevenZMethodConfigurationTest::shouldAllowNumberForDeflate
    public void shouldAllowNumberForDeflate() {
        Assert.assertNotNull(new SevenZMethodConfiguration(SevenZMethod.DEFLATE, 42)
                             .getOptions());
    }

// org.apache.commons.compress.archivers.sevenz.SevenZMethodConfigurationTest::shouldNotAllowStringOptionsForLZMA2
    public void shouldNotAllowStringOptionsForLZMA2() {
        new SevenZMethodConfiguration(SevenZMethod.LZMA2, "");
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDirectoriesAndEmptyFiles
    public void testDirectoriesAndEmptyFiles() throws Exception {
        output = new File(dir, "empties.7z");

        Date accessDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        Date creationDate = cal.getTime();

        SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            SevenZArchiveEntry entry = outArchive.createArchiveEntry(dir, "foo/");
            outArchive.putArchiveEntry(entry);
            outArchive.closeArchiveEntry();

            entry = new SevenZArchiveEntry();
            entry.setName("foo/bar");
            entry.setCreationDate(creationDate);
            entry.setAccessDate(accessDate);
            outArchive.putArchiveEntry(entry);
            outArchive.write(new byte[0]);
            outArchive.closeArchiveEntry();

            entry = new SevenZArchiveEntry();
            entry.setName("xyzzy");
            outArchive.putArchiveEntry(entry);
            outArchive.write(0);
            outArchive.closeArchiveEntry();

            entry = outArchive.createArchiveEntry(dir, "baz/");
            entry.setAntiItem(true);
            outArchive.putArchiveEntry(entry);
            outArchive.closeArchiveEntry();

            entry = new SevenZArchiveEntry();
            entry.setName("dada");
            entry.setHasWindowsAttributes(true);
            entry.setWindowsAttributes(17);
            outArchive.putArchiveEntry(entry);
            outArchive.write(5);
            outArchive.write(42);
            outArchive.closeArchiveEntry();

            outArchive.finish();
        } finally {
            outArchive.close();
        }

        final SevenZFile archive = new SevenZFile(output);
        try {
            SevenZArchiveEntry entry = archive.getNextEntry();
            assert(entry != null);
            assertEquals("foo/", entry.getName());
            assertTrue(entry.isDirectory());
            assertFalse(entry.isAntiItem());

            entry = archive.getNextEntry();
            assert(entry != null);
            assertEquals("foo/bar", entry.getName());
            assertFalse(entry.isDirectory());
            assertFalse(entry.isAntiItem());
            assertEquals(0, entry.getSize());
            assertFalse(entry.getHasLastModifiedDate());
            assertEquals(accessDate, entry.getAccessDate());
            assertEquals(creationDate, entry.getCreationDate());

            entry = archive.getNextEntry();
            assert(entry != null);
            assertEquals("xyzzy", entry.getName());
            assertEquals(1, entry.getSize());
            assertFalse(entry.getHasAccessDate());
            assertFalse(entry.getHasCreationDate());
            assertEquals(0, archive.read());

            entry = archive.getNextEntry();
            assert(entry != null);
            assertEquals("baz/", entry.getName());
            assertTrue(entry.isDirectory());
            assertTrue(entry.isAntiItem());

            entry = archive.getNextEntry();
            assert(entry != null);
            assertEquals("dada", entry.getName());
            assertEquals(2, entry.getSize());
            byte[] content = new byte[2];
            assertEquals(2, archive.read(content));
            assertEquals(5, content[0]);
            assertEquals(42, content[1]);
            assertEquals(17, entry.getWindowsAttributes());

            assert(archive.getNextEntry() == null);
        } finally {
            archive.close();
        }

    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDirectoriesOnly
    public void testDirectoriesOnly() throws Exception {
        output = new File(dir, "dirs.7z");
        SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo/");
            entry.setDirectory(true);
            outArchive.putArchiveEntry(entry);
            outArchive.closeArchiveEntry();
        } finally {
            outArchive.close();
        }

        final SevenZFile archive = new SevenZFile(output);
        try {
            SevenZArchiveEntry entry = archive.getNextEntry();
            assert(entry != null);
            assertEquals("foo/", entry.getName());
            assertTrue(entry.isDirectory());
            assertFalse(entry.isAntiItem());

            assert(archive.getNextEntry() == null);
        } finally {
            archive.close();
        }

    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testCantFinishTwice
    public void testCantFinishTwice() throws Exception {
        output = new File(dir, "finish.7z");
        SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            outArchive.finish();
            outArchive.finish();
            fail("shouldn't be able to call finish twice");
        } catch (IOException ex) {
            assertEquals("This archive has already been finished", ex.getMessage());
        } finally {
            outArchive.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testSixEmptyFiles
    public void testSixEmptyFiles() throws Exception {
        testCompress252(6, 0);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testSixFilesSomeNotEmpty
    public void testSixFilesSomeNotEmpty() throws Exception {
        testCompress252(6, 2);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testSevenEmptyFiles
    public void testSevenEmptyFiles() throws Exception {
        testCompress252(7, 0);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testSevenFilesSomeNotEmpty
    public void testSevenFilesSomeNotEmpty() throws Exception {
        testCompress252(7, 2);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testEightEmptyFiles
    public void testEightEmptyFiles() throws Exception {
        testCompress252(8, 0);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testEightFilesSomeNotEmpty
    public void testEightFilesSomeNotEmpty() throws Exception {
        testCompress252(8, 2);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testNineEmptyFiles
    public void testNineEmptyFiles() throws Exception {
        testCompress252(9, 0);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testNineFilesSomeNotEmpty
    public void testNineFilesSomeNotEmpty() throws Exception {
        testCompress252(9, 2);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testTwentyNineEmptyFiles
    public void testTwentyNineEmptyFiles() throws Exception {
        testCompress252(29, 0);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testTwentyNineFilesSomeNotEmpty
    public void testTwentyNineFilesSomeNotEmpty() throws Exception {
        testCompress252(29, 7);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testCopyRoundtrip
    public void testCopyRoundtrip() throws Exception {
        testRoundTrip(SevenZMethod.COPY);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testBzip2Roundtrip
    public void testBzip2Roundtrip() throws Exception {
        testRoundTrip(SevenZMethod.BZIP2);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testLzma2Roundtrip
    public void testLzma2Roundtrip() throws Exception {
        testRoundTrip(SevenZMethod.LZMA2);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDeflateRoundtrip
    public void testDeflateRoundtrip() throws Exception {
        testRoundTrip(SevenZMethod.DEFLATE);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testBCJX86Roundtrip
    public void testBCJX86Roundtrip() throws Exception {
        if (XZ_BCJ_IS_BUGGY) { return; }
        testFilterRoundTrip(new SevenZMethodConfiguration(SevenZMethod.BCJ_X86_FILTER));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testBCJARMRoundtrip
    public void testBCJARMRoundtrip() throws Exception {
        if (XZ_BCJ_IS_BUGGY) { return; }
        testFilterRoundTrip(new SevenZMethodConfiguration(SevenZMethod.BCJ_ARM_FILTER));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testBCJARMThumbRoundtrip
    public void testBCJARMThumbRoundtrip() throws Exception {
        if (XZ_BCJ_IS_BUGGY) { return; }
        testFilterRoundTrip(new SevenZMethodConfiguration(SevenZMethod.BCJ_ARM_THUMB_FILTER));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testBCJIA64Roundtrip
    public void testBCJIA64Roundtrip() throws Exception {
        if (XZ_BCJ_IS_BUGGY) { return; }
        testFilterRoundTrip(new SevenZMethodConfiguration(SevenZMethod.BCJ_IA64_FILTER));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testBCJPPCRoundtrip
    public void testBCJPPCRoundtrip() throws Exception {
        if (XZ_BCJ_IS_BUGGY) { return; }
        testFilterRoundTrip(new SevenZMethodConfiguration(SevenZMethod.BCJ_PPC_FILTER));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testBCJSparcRoundtrip
    public void testBCJSparcRoundtrip() throws Exception {
        if (XZ_BCJ_IS_BUGGY) { return; }
        testFilterRoundTrip(new SevenZMethodConfiguration(SevenZMethod.BCJ_SPARC_FILTER));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDeltaRoundtrip
    public void testDeltaRoundtrip() throws Exception {
        testFilterRoundTrip(new SevenZMethodConfiguration(SevenZMethod.DELTA_FILTER));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testStackOfContentCompressions
    public void testStackOfContentCompressions() throws Exception {
        output = new File(dir, "multiple-methods.7z");
        ArrayList<SevenZMethodConfiguration> methods = new ArrayList<SevenZMethodConfiguration>();
        methods.add(new SevenZMethodConfiguration(SevenZMethod.LZMA2));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.COPY));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.DEFLATE));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.BZIP2));
        createAndReadBack(output, methods);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDeflateWithConfiguration
    public void testDeflateWithConfiguration() throws Exception {
        output = new File(dir, "deflate-options.7z");
        
        createAndReadBack(output, Collections
                          .singletonList(new SevenZMethodConfiguration(SevenZMethod.DEFLATE, 1)));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testBzip2WithConfiguration
    public void testBzip2WithConfiguration() throws Exception {
        output = new File(dir, "bzip2-options.7z");
        
        createAndReadBack(output, Collections
                          .singletonList(new SevenZMethodConfiguration(SevenZMethod.BZIP2, 4)));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testLzma2WithIntConfiguration
    public void testLzma2WithIntConfiguration() throws Exception {
        output = new File(dir, "lzma2-options.7z");
        
        createAndReadBack(output, Collections
                          .singletonList(new SevenZMethodConfiguration(SevenZMethod.LZMA2, 1 << 20)));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testLzma2WithOptionsConfiguration
    public void testLzma2WithOptionsConfiguration() throws Exception {
        output = new File(dir, "lzma2-options2.7z");
        LZMA2Options opts = new LZMA2Options(1);
        createAndReadBack(output, Collections
                          .singletonList(new SevenZMethodConfiguration(SevenZMethod.LZMA2, opts)));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testArchiveWithMixedMethods
    public void testArchiveWithMixedMethods() throws Exception {
        output = new File(dir, "mixed-methods.7z");
        SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            addFile(outArchive, 0, true);
            addFile(outArchive, 1, true, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.BZIP2)));
        } finally {
            outArchive.close();
        }

        SevenZFile archive = new SevenZFile(output);
        try {
            assertEquals(Boolean.TRUE,
                         verifyFile(archive, 0, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.LZMA2))));
            assertEquals(Boolean.TRUE,
                         verifyFile(archive, 1, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.BZIP2))));
        } finally {
            archive.close();
        }
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testBzipCreation
    public void testBzipCreation()  throws Exception {
        File output = null;
        final File input = getFile("test.txt");
        {
            output = new File(dir, "test.txt.bz2");
            final OutputStream out = new FileOutputStream(output);
            final CompressorOutputStream cos = new CompressorStreamFactory().createCompressorOutputStream("bzip2", out);
            FileInputStream in = new FileInputStream(input);
            IOUtils.copy(in, cos);
            cos.close();
            in.close();
        }

        final File decompressed = new File(dir, "decompressed.txt");
        {
            final File toDecompress = output;
            final InputStream is = new FileInputStream(toDecompress);
            final CompressorInputStream in =
                new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
            FileOutputStream os = new FileOutputStream(decompressed);
            IOUtils.copy(in, os);
            is.close();
            os.close();
        }

        assertEquals(input.length(),decompressed.length());
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testBzip2Unarchive
    public void testBzip2Unarchive() throws Exception {
        final File input = getFile("bla.txt.bz2");
        final File output = new File(dir, "bla.txt");
        final InputStream is = new FileInputStream(input);
        final CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
        FileOutputStream os = new FileOutputStream(output);
        IOUtils.copy(in, os);
        is.close();
        os.close();
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testConcatenatedStreamsReadFirstOnly
    public void testConcatenatedStreamsReadFirstOnly() throws Exception {
        final File input = getFile("multiple.bz2");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in = new CompressorStreamFactory()
                .createCompressorInputStream("bzip2", is);
            try {
                assertEquals('a', in.read());
                assertEquals(-1, in.read());
            } finally {
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testConcatenatedStreamsReadFully
    public void testConcatenatedStreamsReadFully() throws Exception {
        final File input = getFile("multiple.bz2");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in =
                new BZip2CompressorInputStream(is, true);
            try {
                assertEquals('a', in.read());
                assertEquals('b', in.read());
                assertEquals(0, in.available());
                assertEquals(-1, in.read());
            } finally {
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testCOMPRESS131
    public void testCOMPRESS131() throws Exception {
        final File input = getFile("COMPRESS-131.bz2");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in =
                new BZip2CompressorInputStream(is, true);
            try {
                int l = 0;
                while(in.read() != -1) {
                    l++;
                }
                assertEquals(539, l);
            } finally {
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.DeflateTestCase::testDeflateCreation
    public void testDeflateCreation()  throws Exception {
        final File input = getFile("test1.xml");
        final File output = new File(dir, "test1.xml.deflatez");
        final OutputStream out = new FileOutputStream(output);
        try {
            final CompressorOutputStream cos = new CompressorStreamFactory()
                .createCompressorOutputStream("deflate", out); 
            try {
                IOUtils.copy(new FileInputStream(input), cos);
            } finally {
                cos.close();
            }
        } finally {
            out.close();
        }
    }

// org.apache.commons.compress.compressors.DeflateTestCase::testRawDeflateCreation
    public void testRawDeflateCreation()  throws Exception {
        final File input = getFile("test1.xml");
        final File output = new File(dir, "test1.xml.deflate");
        final OutputStream out = new FileOutputStream(output);
        try {
            DeflateParameters params = new DeflateParameters();
            params.setWithZlibHeader(false);
            final CompressorOutputStream cos = new DeflateCompressorOutputStream(out, params);
            try {
                IOUtils.copy(new FileInputStream(input), cos);
            } finally {
                cos.close();
            }
        } finally {
            out.close();
        }
    }

// org.apache.commons.compress.compressors.DeflateTestCase::testDeflateUnarchive
    public void testDeflateUnarchive() throws Exception {
        final File input = getFile("bla.tar.deflatez");
        final File output = new File(dir, "bla.tar");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in = new CompressorStreamFactory()
                .createCompressorInputStream("deflate", is); 
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(output);
                IOUtils.copy(in, out);
            } finally {
                if (out != null) {
                    out.close();
                }
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.DeflateTestCase::testRawDeflateUnarchive
    public void testRawDeflateUnarchive() throws Exception {
        final File input = getFile("bla.tar.deflate");
        final File output = new File(dir, "bla.tar");
        final InputStream is = new FileInputStream(input);
        try {
            DeflateParameters params = new DeflateParameters();
            params.setWithZlibHeader(false);
            final CompressorInputStream in = new DeflateCompressorInputStream(is, params);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(output);
                IOUtils.copy(in, out);
            } finally {
                if (out != null) {
                    out.close();
                }
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testDetection
    public void testDetection() throws Exception {
        CompressorInputStream bzip2 = getStreamFor("bla.txt.bz2"); 
        assertNotNull(bzip2);
        assertTrue(bzip2 instanceof BZip2CompressorInputStream);

        CompressorInputStream gzip = getStreamFor("bla.tgz");
        assertNotNull(gzip);
        assertTrue(gzip instanceof GzipCompressorInputStream);
        
        CompressorInputStream pack200 = getStreamFor("bla.pack");
        assertNotNull(pack200);
        assertTrue(pack200 instanceof Pack200CompressorInputStream);

        CompressorInputStream xz = getStreamFor("bla.tar.xz");
        assertNotNull(xz);
        assertTrue(xz instanceof XZCompressorInputStream);

        try {
            factory.createCompressorInputStream(new ByteArrayInputStream(new byte[0]));
            fail("No exception thrown for an empty input stream");
        } catch (CompressorException e) {
            
        }
    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testOverride
    public void testOverride() {
        CompressorStreamFactory fac = new CompressorStreamFactory();
        assertFalse(fac.getDecompressConcatenated());
        fac.setDecompressConcatenated(true);
        assertTrue(fac.getDecompressConcatenated());

        fac = new CompressorStreamFactory(false);
        assertFalse(fac.getDecompressConcatenated());
        try {
            fac.setDecompressConcatenated(true);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }

        fac = new CompressorStreamFactory(true);
        assertTrue(fac.getDecompressConcatenated());
        try {
            fac.setDecompressConcatenated(true);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ise) {
            
        }
    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testMutiples
    public void testMutiples() throws Exception {
        for(int i=0; i <tests.length; i++) {
            TestData test = tests[i];
            final CompressorStreamFactory fac = test.factory;
            assertNotNull("Test entry "+i, fac);
            assertEquals("Test entry "+i, test.concat, fac.getDecompressConcatenated());
            CompressorInputStream in = getStreamFor(test.fileName, fac);
            assertNotNull("Test entry "+i,in);
            for (char entry : test.entryNames) {
                assertEquals("Test entry" + i, entry, in.read());                
            }
            assertEquals(0, in.available());
            assertEquals(-1, in.read());
        }
    }

// org.apache.commons.compress.compressors.FramedSnappyTestCase::testDefaultExtraction
    public void testDefaultExtraction() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            public CompressorInputStream wrap(InputStream is) throws IOException {
                return new FramedSnappyCompressorInputStream(is);
            }
        });
    }

// org.apache.commons.compress.compressors.FramedSnappyTestCase::testDefaultExtractionViaFactory
    public void testDefaultExtractionViaFactory() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            public CompressorInputStream wrap(InputStream is) throws Exception {
                return new CompressorStreamFactory()
                    .createCompressorInputStream(CompressorStreamFactory.SNAPPY_FRAMED,
                                                 is);
            }
        });
    }

// org.apache.commons.compress.compressors.FramedSnappyTestCase::testDefaultExtractionViaFactoryAutodetection
    public void testDefaultExtractionViaFactoryAutodetection() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            public CompressorInputStream wrap(InputStream is) throws Exception {
                return new CompressorStreamFactory().createCompressorInputStream(is);
            }
        });
    }

// org.apache.commons.compress.compressors.GZipTestCase::testGzipCreation
    public void testGzipCreation()  throws Exception {
        final File input = getFile("test1.xml");
        final File output = new File(dir, "test1.xml.gz");
        final OutputStream out = new FileOutputStream(output);
        try {
            final CompressorOutputStream cos = new CompressorStreamFactory()
                .createCompressorOutputStream("gz", out);
            try {
                IOUtils.copy(new FileInputStream(input), cos);
            } finally {
                cos.close();
            }
        } finally {
            out.close();
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testGzipUnarchive
    public void testGzipUnarchive() throws Exception {
        final File input = getFile("bla.tgz");
        final File output = new File(dir, "bla.tar");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in = new CompressorStreamFactory()
                .createCompressorInputStream("gz", is);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(output);
                IOUtils.copy(in, out);
            } finally {
                if (out != null) {
                    out.close();
                }
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testConcatenatedStreamsReadFirstOnly
    public void testConcatenatedStreamsReadFirstOnly() throws Exception {
        final File input = getFile("multiple.gz");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in = new CompressorStreamFactory()
                .createCompressorInputStream("gz", is);
            try {
                assertEquals('a', in.read());
                assertEquals(-1, in.read());
            } finally {
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testConcatenatedStreamsReadFully
    public void testConcatenatedStreamsReadFully() throws Exception {
        final File input = getFile("multiple.gz");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in =
                new GzipCompressorInputStream(is, true);
            try {
                assertEquals('a', in.read());
                assertEquals('b', in.read());
                assertEquals(0, in.available());
                assertEquals(-1, in.read());
            } finally {
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testCorruptedInput
    public void testCorruptedInput() throws Exception {
        InputStream in = null;
        OutputStream out = null;
        CompressorInputStream cin = null;
        try {
            in = new FileInputStream(getFile("bla.tgz"));
            out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            in.close();
            out.close();

            byte[] data = ((ByteArrayOutputStream) out).toByteArray();
            in = new ByteArrayInputStream(data, 0, data.length - 1);
            cin = new CompressorStreamFactory()
                .createCompressorInputStream("gz", in);
            out = new ByteArrayOutputStream();

            try {
                IOUtils.copy(cin, out);
                fail("Expected an exception");
            } catch (IOException ioex) {
                
            }

        } finally {
            if (out != null) {
                out.close();
            }
            if (cin != null) {
                cin.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testInteroperabilityWithGzipCompressorInputStream
    public void testInteroperabilityWithGzipCompressorInputStream() throws Exception {
        FileInputStream fis = new FileInputStream(getFile("test3.xml"));
        byte[] content;
        try {
            content = IOUtils.toByteArray(fis);
        } finally {
            fis.close();
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
        parameters.setOperatingSystem(3);
        parameters.setFilename("test3.xml");
        parameters.setComment("Test file");
        parameters.setModificationTime(System.currentTimeMillis());
        GzipCompressorOutputStream out = new GzipCompressorOutputStream(bout, parameters);
        out.write(content);
        out.flush();
        out.close();

        GzipCompressorInputStream in = new GzipCompressorInputStream(new ByteArrayInputStream(bout.toByteArray()));
        byte[] content2 = IOUtils.toByteArray(in);

        Assert.assertArrayEquals("uncompressed content", content, content2);
    }

// org.apache.commons.compress.compressors.GZipTestCase::testInteroperabilityWithGZIPInputStream
    public void testInteroperabilityWithGZIPInputStream() throws Exception {
        FileInputStream fis = new FileInputStream(getFile("test3.xml"));
        byte[] content;
        try {
            content = IOUtils.toByteArray(fis);
        } finally {
            fis.close();
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
        parameters.setOperatingSystem(3);
        parameters.setFilename("test3.xml");
        parameters.setComment("Test file");
        parameters.setModificationTime(System.currentTimeMillis());
        GzipCompressorOutputStream out = new GzipCompressorOutputStream(bout, parameters);
        out.write(content);
        out.flush();
        out.close();

        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        byte[] content2 = IOUtils.toByteArray(in);

        Assert.assertArrayEquals("uncompressed content", content, content2);
    }

// org.apache.commons.compress.compressors.GZipTestCase::testInvalidCompressionLevel
    public void testInvalidCompressionLevel() {
        GzipParameters parameters = new GzipParameters();
        try {
            parameters.setCompressionLevel(10);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            
        }
        
        try {
            parameters.setCompressionLevel(-5);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testExtraFlagsFastestCompression
    public void testExtraFlagsFastestCompression() throws Exception {
        testExtraFlags(Deflater.BEST_SPEED, 4);
    }

// org.apache.commons.compress.compressors.GZipTestCase::testExtraFlagsBestCompression
    public void testExtraFlagsBestCompression() throws Exception {
        testExtraFlags(Deflater.BEST_COMPRESSION, 2);
    }

// org.apache.commons.compress.compressors.GZipTestCase::testExtraFlagsDefaultCompression
    public void testExtraFlagsDefaultCompression() throws Exception {
        testExtraFlags(Deflater.DEFAULT_COMPRESSION, 0);
    }

// org.apache.commons.compress.compressors.GZipTestCase::testOverWrite
    public void testOverWrite() throws Exception {
        GzipCompressorOutputStream out = new GzipCompressorOutputStream(new ByteArrayOutputStream());
        out.close();
        try {
            out.write(0);
            fail("IOException expected");
        } catch (IOException e) {
            
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testMetadataRoundTrip
    public void testMetadataRoundTrip() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                
        GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
        parameters.setModificationTime(123456000);
        parameters.setOperatingSystem(13);
        parameters.setFilename("test3.xml");
        parameters.setComment("Umlaute möglich?");
        GzipCompressorOutputStream out = new GzipCompressorOutputStream(bout, parameters);
        FileInputStream fis = new FileInputStream(getFile("test3.xml"));
        try {
            IOUtils.copy(fis, out);
        } finally {
            fis.close();
            out.close();
        }
        
        GzipCompressorInputStream input =
            new GzipCompressorInputStream(new ByteArrayInputStream(bout.toByteArray()));
        input.close();
        GzipParameters readParams = input.getMetaData();
        assertEquals(Deflater.BEST_COMPRESSION, readParams.getCompressionLevel());
        assertEquals(123456000, readParams.getModificationTime());
        assertEquals(13, readParams.getOperatingSystem());
        assertEquals("test3.xml", readParams.getFilename());
        assertEquals("Umlaute möglich?", readParams.getComment());
    }

// org.apache.commons.compress.compressors.XZTestCase::testXZCreation
    public void testXZCreation()  throws Exception {
        long max = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
        System.out.println("XZTestCase: HeapMax="+max+" bytes "+(double)max/(1024*1024)+" MB");
        final File input = getFile("test1.xml");
        final File output = new File(dir, "test1.xml.xz");
        final OutputStream out = new FileOutputStream(output);
        try {
            final CompressorOutputStream cos = new CompressorStreamFactory()
                .createCompressorOutputStream("xz", out);
            try {
                IOUtils.copy(new FileInputStream(input), cos);
            } finally {
                cos.close();
            }
        } finally {
            out.close();
        }
    }

// org.apache.commons.compress.compressors.XZTestCase::testXZUnarchive
    public void testXZUnarchive() throws Exception {
        final File input = getFile("bla.tar.xz");
        final File output = new File(dir, "bla.tar");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in = new CompressorStreamFactory()
                .createCompressorInputStream("xz", is);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(output);
                IOUtils.copy(in, out);
            } finally {
                if (out != null) {
                    out.close();
                }
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.XZTestCase::testConcatenatedStreamsReadFirstOnly
    public void testConcatenatedStreamsReadFirstOnly() throws Exception {
        final File input = getFile("multiple.xz");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in = new CompressorStreamFactory()
                .createCompressorInputStream("xz", is);
            try {
                assertEquals('a', in.read());
                assertEquals(-1, in.read());
            } finally {
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.XZTestCase::testConcatenatedStreamsReadFully
    public void testConcatenatedStreamsReadFully() throws Exception {
        final File input = getFile("multiple.xz");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in =
                new XZCompressorInputStream(is, true);
            try {
                assertEquals('a', in.read());
                assertEquals('b', in.read());
                assertEquals(0, in.available());
                assertEquals(-1, in.read());
            } finally {
                in.close();
            }
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.ZTestCase::testZUnarchive
    public void testZUnarchive() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            public CompressorInputStream wrap(InputStream is) throws IOException {
                return new ZCompressorInputStream(is);
            }
        });
    }

// org.apache.commons.compress.compressors.ZTestCase::testZUnarchiveViaFactory
    public void testZUnarchiveViaFactory() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            public CompressorInputStream wrap(InputStream is) throws Exception {
                return new CompressorStreamFactory()
                    .createCompressorInputStream(CompressorStreamFactory.Z, is);
            }
        });
    }

// org.apache.commons.compress.compressors.ZTestCase::testZUnarchiveViaAutoDetection
    public void testZUnarchiveViaAutoDetection() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            public CompressorInputStream wrap(InputStream is) throws Exception {
                return new CompressorStreamFactory()
                    .createCompressorInputStream(new BufferedInputStream(is));
            }
        });
    }

// org.apache.commons.compress.compressors.ZTestCase::testMatches
    public void testMatches() throws Exception {
        assertFalse(ZCompressorInputStream.matches(new byte[] { 1, 2, 3, 4 }, 4));
        assertFalse(ZCompressorInputStream.matches(new byte[] { 0x1f, 2, 3, 4 }, 4));
        assertFalse(ZCompressorInputStream.matches(new byte[] { 1, (byte)0x9d, 3, 4 },
                                                   4));
        assertFalse(ZCompressorInputStream.matches(new byte[] { 0x1f, (byte) 0x9d, 3, 4 },
                                                   3));
        assertTrue(ZCompressorInputStream.matches(new byte[] { 0x1f, (byte) 0x9d, 3, 4 },
                                                  4));
    }

// org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStreamTest::shouldThrowAnIOExceptionWhenAppliedToAZipFile
    public void shouldThrowAnIOExceptionWhenAppliedToAZipFile() throws Exception {
        FileInputStream in = new FileInputStream(getFile("bla.zip"));
        try {
            new BZip2CompressorInputStream(in);
        } finally {
            in.close();
        }
    }

// org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStreamTest::readOfLength0ShouldReturn0
    public void readOfLength0ShouldReturn0() throws Exception {
        
        byte[] rawData = new byte[1048576];
        for (int i=0; i < rawData.length; ++i) {
            rawData[i] = (byte) Math.floor(Math.random()*256);
        }

        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzipOut = new BZip2CompressorOutputStream(baos);
        bzipOut.write(rawData);
        bzipOut.flush();
        bzipOut.close();
        baos.flush();
        baos.close();

        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        BZip2CompressorInputStream bzipIn = new BZip2CompressorInputStream(bais);
        byte[] buffer = new byte[1024];
        Assert.assertEquals(1024, bzipIn.read(buffer, 0, 1024));
        Assert.assertEquals(0, bzipIn.read(buffer, 1024, 0));
        Assert.assertEquals(1024, bzipIn.read(buffer, 0, 1024));
        bzipIn.close();
    }

// org.apache.commons.compress.compressors.bzip2.PythonTruncatedBzip2Test::testTruncatedData
    public void testTruncatedData() throws IOException {
        
        
        System.out.println("Attempt to read the whole thing in, should throw ...");
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        bz2Channel.read(buffer);
    }

// org.apache.commons.compress.compressors.bzip2.PythonTruncatedBzip2Test::testPartialReadTruncatedData
    public void testPartialReadTruncatedData() throws IOException {
        
        
        

        final int length = TEXT.length();
        ByteBuffer buffer = ByteBuffer.allocate(length);
        bz2Channel.read(buffer);

        assertArrayEquals(copyOfRange(TEXT.getBytes(), 0, length),
                buffer.array());

        
        buffer = ByteBuffer.allocate(1);
        try {
            bz2Channel.read(buffer);
            Assert.fail("The read should have thrown.");
        } catch (IOException e) {
            
        }
    }
