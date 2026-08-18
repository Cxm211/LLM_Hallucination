// buggy code
    public CompressorInputStream createCompressorInputStream(final InputStream in)
            throws CompressorException {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }

        if (!in.markSupported()) {
            throw new IllegalArgumentException("Mark is not supported.");
        }

        final byte[] signature = new byte[12];
        in.mark(signature.length);
        try {
            int signatureLength = IOUtils.readFully(in, signature);
            in.reset();

            if (BZip2CompressorInputStream.matches(signature, signatureLength)) {
                return new BZip2CompressorInputStream(in, decompressConcatenated);
            }

            if (GzipCompressorInputStream.matches(signature, signatureLength)) {
                return new GzipCompressorInputStream(in, decompressConcatenated);
            }

            if (Pack200CompressorInputStream.matches(signature, signatureLength)) {
                return new Pack200CompressorInputStream(in);
            }

            if (FramedSnappyCompressorInputStream.matches(signature, signatureLength)) {
                return new FramedSnappyCompressorInputStream(in);
            }

            if (ZCompressorInputStream.matches(signature, signatureLength)) {
                return new ZCompressorInputStream(in);
            }


            if (XZUtils.matches(signature, signatureLength) &&
                XZUtils.isXZCompressionAvailable()) {
                return new XZCompressorInputStream(in, decompressConcatenated);
            }

            if (LZMAUtils.matches(signature, signatureLength) &&
                LZMAUtils.isLZMACompressionAvailable()) {
                return new LZMACompressorInputStream(in);
            }

        } catch (IOException e) {
            throw new CompressorException("Failed to detect Compressor from InputStream.", e);
        }

        throw new CompressorException("No Compressor found for the stream signature.");
    }

    public void close() throws IOException {
        in.close();
    }

// relevant test
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

        CompressorInputStream zlib = getStreamFor("bla.tar.deflatez");
        assertNotNull(zlib);
        assertTrue(zlib instanceof DeflateCompressorInputStream);

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

// org.apache.commons.compress.compressors.LZMATestCase::testLZMAUnarchive
    public void testLZMAUnarchive() throws Exception {
        final File input = getFile("bla.tar.lzma");
        final File output = new File(dir, "bla.tar");
        final InputStream is = new FileInputStream(input);
        try {
            final CompressorInputStream in = new LZMACompressorInputStream(is);
            copy(in, output);
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.LZMATestCase::testLZMAUnarchiveWithAutodetection
    public void testLZMAUnarchiveWithAutodetection() throws Exception {
        final File input = getFile("bla.tar.lzma");
        final File output = new File(dir, "bla.tar");
        final InputStream is = new BufferedInputStream(new FileInputStream(input));
        try {
            final CompressorInputStream in = new CompressorStreamFactory()
                .createCompressorInputStream(is);
            copy(in, output);
        } finally {
            is.close();
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

// org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStreamTest::availableShouldReturnNonZero
    public void availableShouldReturnNonZero() throws IOException {
        final File input = AbstractTestCase.getFile("bla.tar.deflatez");
        final InputStream is = new FileInputStream(input);
        try {
            DeflateCompressorInputStream in =
                new DeflateCompressorInputStream(is);
            Assert.assertTrue(in.available() > 0);
            in.close();
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStreamTest::shouldBeAbleToSkipAByte
    public void shouldBeAbleToSkipAByte() throws IOException {
        final File input = AbstractTestCase.getFile("bla.tar.deflatez");
        final InputStream is = new FileInputStream(input);
        try {
            DeflateCompressorInputStream in =
                new DeflateCompressorInputStream(is);
            Assert.assertEquals(1, in.skip(1));
            in.close();
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStreamTest::singleByteReadWorksAsExpected
    public void singleByteReadWorksAsExpected() throws IOException {
        final File input = AbstractTestCase.getFile("bla.tar.deflatez");
        final InputStream is = new FileInputStream(input);
        try {
            DeflateCompressorInputStream in =
                new DeflateCompressorInputStream(is);
            
            Assert.assertEquals('t', in.read());
            in.close();
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStreamTest::singleByteReadReturnsMinusOneAtEof
    public void singleByteReadReturnsMinusOneAtEof() throws IOException {
        final File input = AbstractTestCase.getFile("bla.tar.deflatez");
        final InputStream is = new FileInputStream(input);
        try {
            DeflateCompressorInputStream in =
                new DeflateCompressorInputStream(is);
            IOUtils.toByteArray(in);
            Assert.assertEquals(-1, in.read());
            in.close();
        } finally {
            is.close();
        }
    }
