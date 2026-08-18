// buggy code
    public BZip2CompressorInputStream(final InputStream in, final boolean decompressConcatenated) throws IOException {
        this.in = in;
        this.decompressConcatenated = decompressConcatenated;

        init(true);
        initBlock();
        setupBlock();
    }

    private int read0() throws IOException {
        final int retChar = this.currentChar;
        switch (currentState) {
        case EOF:
            return -1;

        case START_BLOCK_STATE:
            throw new IllegalStateException();

        case RAND_PART_A_STATE:
            throw new IllegalStateException();

        case RAND_PART_B_STATE:
            setupRandPartB();
            break;

        case RAND_PART_C_STATE:
            setupRandPartC();
            break;

        case NO_RAND_PART_A_STATE:
            throw new IllegalStateException();

        case NO_RAND_PART_B_STATE:
            setupNoRandPartB();
            break;

        case NO_RAND_PART_C_STATE:
            setupNoRandPartC();
            break;

        default:
            throw new IllegalStateException();
        }
        return retChar;
    }

    private int setupRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {
            this.su_chPrev = this.su_ch2;
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;
            this.su_tPos = this.data.tt[this.su_tPos];
            if (this.su_rNToGo == 0) {
                this.su_rNToGo = Rand.rNums(this.su_rTPos) - 1;
                if (++this.su_rTPos == 512) {
                    this.su_rTPos = 0;
                }
            } else {
                this.su_rNToGo--;
            }
            this.su_ch2 = su_ch2Shadow ^= (this.su_rNToGo == 1) ? 1 : 0;
            this.su_i2++;
            this.currentChar = su_ch2Shadow;
            this.currentState = RAND_PART_B_STATE;
            this.crc.updateCRC(su_ch2Shadow);
            return su_ch2Shadow;
        } else {
            endBlock();
            initBlock();
            return setupBlock();
        }
    }

    private int setupNoRandPartA() throws IOException {
        if (this.su_i2 <= this.last) {
            this.su_chPrev = this.su_ch2;
            int su_ch2Shadow = this.data.ll8[this.su_tPos] & 0xff;
            this.su_ch2 = su_ch2Shadow;
            this.su_tPos = this.data.tt[this.su_tPos];
            this.su_i2++;
            this.currentChar = su_ch2Shadow;
            this.currentState = NO_RAND_PART_B_STATE;
            this.crc.updateCRC(su_ch2Shadow);
            return su_ch2Shadow;
        } else {
            this.currentState = NO_RAND_PART_A_STATE;
            endBlock();
            initBlock();
            return setupBlock();
        }
    }

    private int setupRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {
            this.currentChar = this.su_ch2;
            this.crc.updateCRC(this.su_ch2);
            this.su_j2++;
            return this.su_ch2;
        } else {
            this.currentState = RAND_PART_A_STATE;
            this.su_i2++;
            this.su_count = 0;
            return setupRandPartA();
        }
    }

    private int setupNoRandPartC() throws IOException {
        if (this.su_j2 < this.su_z) {
            int su_ch2Shadow = this.su_ch2;
            this.currentChar = su_ch2Shadow;
            this.crc.updateCRC(su_ch2Shadow);
            this.su_j2++;
            this.currentState = NO_RAND_PART_C_STATE;
            return su_ch2Shadow;
        } else {
            this.su_i2++;
            this.su_count = 0;
            return setupNoRandPartA();
        }
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

// org.apache.commons.compress.DetectCompressorTestCase::testDetection
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
        test7zUnarchive(getFile("bla.7z"));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::test7zDeflateUnarchive
    public void test7zDeflateUnarchive() throws Exception {
        test7zUnarchive(getFile("bla.deflate.7z"));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::test7zDecryptUnarchive
    public void test7zDecryptUnarchive() throws Exception {
        if (isStrongCryptoAvailable()) {
            test7zUnarchive(getFile("bla.encrypted.7z"), "foo".getBytes("UTF-16LE"));
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testEncryptedArchiveRequiresPassword
    public void testEncryptedArchiveRequiresPassword() throws Exception {
        try {
            new SevenZFile(getFile("bla.encrypted.7z"));
            fail("shouldn't decrypt without a password");
        } catch (IOException ex) {
            assertEquals("Cannot read encrypted files without a password",
                         ex.getMessage());
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDirectoriesAndEmptyFiles
    public void testDirectoriesAndEmptyFiles() throws Exception {
        File output = new File(dir, "empties.7z");

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
        File output = new File(dir, "dirs.7z");
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
        File output = new File(dir, "finish.7z");
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

        assertArrayEquals(Arrays.copyOfRange(TEXT.getBytes(), 0, length),
                buffer.array());

        
        buffer = ByteBuffer.allocate(1);
        try {
            bz2Channel.read(buffer);
            Assert.fail("The read should have thrown.");
        } catch (IOException e) {
            
        }
    }
