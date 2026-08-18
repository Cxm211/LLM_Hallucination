// buggy code
    public ChecksumCalculatingInputStream(final Checksum checksum, final InputStream in) {



        this.checksum = checksum;
        this.in = in;
    }

// relevant test
// org.apache.commons.compress.compressors.DetectCompressorTestCase::testDetection
    public void testDetection() throws Exception {
        final CompressorInputStream bzip2 = getStreamFor("bla.txt.bz2"); 
        assertNotNull(bzip2);
        assertTrue(bzip2 instanceof BZip2CompressorInputStream);

        final CompressorInputStream gzip = getStreamFor("bla.tgz");
        assertNotNull(gzip);
        assertTrue(gzip instanceof GzipCompressorInputStream);
        
        final CompressorInputStream pack200 = getStreamFor("bla.pack");
        assertNotNull(pack200);
        assertTrue(pack200 instanceof Pack200CompressorInputStream);

        final CompressorInputStream xz = getStreamFor("bla.tar.xz");
        assertNotNull(xz);
        assertTrue(xz instanceof XZCompressorInputStream);

        final CompressorInputStream zlib = getStreamFor("bla.tar.deflatez");
        assertNotNull(zlib);
        assertTrue(zlib instanceof DeflateCompressorInputStream);

        try {
            factory.createCompressorInputStream(new ByteArrayInputStream(new byte[0]));
            fail("No exception thrown for an empty input stream");
        } catch (final CompressorException e) {
            
        }
    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testDetect
    public void testDetect() throws Exception {

        assertEquals(CompressorStreamFactory.BZIP2, detect("bla.txt.bz2"));
        assertEquals(CompressorStreamFactory.GZIP, detect("bla.tgz"));
        assertEquals(CompressorStreamFactory.PACK200, detect("bla.pack"));
        assertEquals(CompressorStreamFactory.XZ, detect("bla.tar.xz"));
        assertEquals(CompressorStreamFactory.DEFLATE, detect("bla.tar.deflatez"));
        assertEquals(CompressorStreamFactory.LZ4_FRAMED, detect("bla.tar.lz4"));
        assertEquals(CompressorStreamFactory.LZMA, detect("bla.tar.lzma"));
        assertEquals(CompressorStreamFactory.SNAPPY_FRAMED, detect("bla.tar.sz"));
        assertEquals(CompressorStreamFactory.Z, detect("bla.tar.Z"));

        
        assertEquals(CompressorStreamFactory.Z, detect("COMPRESS-386"));
        assertEquals(CompressorStreamFactory.LZMA, detect("COMPRESS-382"));

        try {
            CompressorStreamFactory.detect(new BufferedInputStream(new ByteArrayInputStream(new byte[0])));
            fail("shouldn't be able to detect empty stream");
        } catch (CompressorException e) {
            assertEquals("No Compressor found for the stream signature.", e.getMessage());
        }

        try {
            CompressorStreamFactory.detect(null);
            fail("shouldn't be able to detect null stream");
        } catch (IllegalArgumentException e) {
            assertEquals("Stream must not be null.", e.getMessage());
        }

        try {
            CompressorStreamFactory.detect(new BufferedInputStream(new MockEvilInputStream()));
            fail("Expected IOException");
        } catch (CompressorException e) {
            assertEquals("IOException while reading signature.", e.getMessage());
        }

    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testLZMAMemoryLimit
    public void testLZMAMemoryLimit() throws Exception {
        getStreamFor("COMPRESS-382", 100);
    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testZMemoryLimit
    public void testZMemoryLimit() throws Exception {
        getStreamFor("COMPRESS-386", 100);
    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testXZMemoryLimitOnRead
    public void testXZMemoryLimitOnRead() throws Exception {
        
        

        
        
        
        try (InputStream compressorIs = getStreamFor("bla.tar.xz", 100)) {
            compressorIs.read();
        }
    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testXZMemoryLimitOnSkip
    public void testXZMemoryLimitOnSkip() throws Exception {
        try (InputStream compressorIs = getStreamFor("bla.tar.xz", 100)) {
            compressorIs.skip(10);
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
        } catch (final IllegalStateException ise) {
            
        }

        fac = new CompressorStreamFactory(true);
        assertTrue(fac.getDecompressConcatenated());
        try {
            fac.setDecompressConcatenated(true);
            fail("Expected IllegalStateException");
        } catch (final IllegalStateException ise) {
            
        }
    }

// org.apache.commons.compress.compressors.DetectCompressorTestCase::testMutiples
    public void testMutiples() throws Exception {
        for(int i=0; i <tests.length; i++) {
            final TestData test = tests[i];
            final CompressorStreamFactory fac = test.factory;
            assertNotNull("Test entry "+i, fac);
            assertEquals("Test entry "+i, test.concat, fac.getDecompressConcatenated());
            final CompressorInputStream in = getStreamFor(test.fileName, fac);
            assertNotNull("Test entry "+i,in);
            for (final char entry : test.entryNames) {
                assertEquals("Test entry" + i, entry, in.read());                
            }
            assertEquals(0, in.available());
            assertEquals(-1, in.read());
        }
    }

// org.apache.commons.compress.compressors.lz4.FactoryTest::frameRoundtripViaFactory
    public void frameRoundtripViaFactory() throws Exception {
        roundtripViaFactory(CompressorStreamFactory.getLZ4Framed());
    }

// org.apache.commons.compress.compressors.lz4.FactoryTest::blockRoundtripViaFactory
    public void blockRoundtripViaFactory() throws Exception {
        roundtripViaFactory(CompressorStreamFactory.getLZ4Block());
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::testMatches
    public void testMatches() throws IOException {
        assertFalse(FramedLZ4CompressorInputStream.matches(new byte[10], 4));
        final byte[] b = new byte[12];
        final File input = getFile("bla.tar.lz4");
        try (FileInputStream in = new FileInputStream(input)) {
            IOUtils.readFully(in, b);
        }
        assertFalse(FramedLZ4CompressorInputStream.matches(b, 3));
        assertTrue(FramedLZ4CompressorInputStream.matches(b, 4));
        assertTrue(FramedLZ4CompressorInputStream.matches(b, 5));
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readBlaLz4
    public void readBlaLz4() throws IOException {
        try (InputStream a = new FramedLZ4CompressorInputStream(new FileInputStream(getFile("bla.tar.lz4")));
            FileInputStream e = new FileInputStream(getFile("bla.tar"))) {
            byte[] expected = IOUtils.toByteArray(e);
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readBlaLz4ViaFactory
    public void readBlaLz4ViaFactory() throws Exception {
        try (InputStream a = new CompressorStreamFactory()
                 .createCompressorInputStream(CompressorStreamFactory.getLZ4Framed(),
                                              new FileInputStream(getFile("bla.tar.lz4")));
            FileInputStream e = new FileInputStream(getFile("bla.tar"))) {
            byte[] expected = IOUtils.toByteArray(e);
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readBlaLz4ViaFactoryAutoDetection
    public void readBlaLz4ViaFactoryAutoDetection() throws Exception {
        try (InputStream a = new CompressorStreamFactory()
                 .createCompressorInputStream(new BufferedInputStream(new FileInputStream(getFile("bla.tar.lz4"))));
            FileInputStream e = new FileInputStream(getFile("bla.tar"))) {
            byte[] expected = IOUtils.toByteArray(e);
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readBlaLz4WithDecompressConcatenated
    public void readBlaLz4WithDecompressConcatenated() throws IOException {
        try (InputStream a = new FramedLZ4CompressorInputStream(new FileInputStream(getFile("bla.tar.lz4")), true);
            FileInputStream e = new FileInputStream(getFile("bla.tar"))) {
            byte[] expected = IOUtils.toByteArray(e);
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readDoubledBlaLz4WithDecompressConcatenatedTrue
    public void readDoubledBlaLz4WithDecompressConcatenatedTrue() throws Exception {
        readDoubledBlaLz4(new StreamWrapper() {
                @Override
                public InputStream wrap(InputStream in) throws Exception {
                    return new FramedLZ4CompressorInputStream(in, true);
                }
            }, true);
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readDoubledBlaLz4WithDecompressConcatenatedFalse
    public void readDoubledBlaLz4WithDecompressConcatenatedFalse() throws Exception {
        readDoubledBlaLz4(new StreamWrapper() {
                @Override
                public InputStream wrap(InputStream in) throws Exception {
                    return new FramedLZ4CompressorInputStream(in, false);
                }
            }, false);
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readDoubledBlaLz4WithoutExplicitDecompressConcatenated
    public void readDoubledBlaLz4WithoutExplicitDecompressConcatenated() throws Exception {
        readDoubledBlaLz4(new StreamWrapper() {
                @Override
                public InputStream wrap(InputStream in) throws Exception {
                    return new FramedLZ4CompressorInputStream(in);
                }
            }, false);
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readBlaLz4ViaFactoryWithDecompressConcatenated
    public void readBlaLz4ViaFactoryWithDecompressConcatenated() throws Exception {
        try (InputStream a = new CompressorStreamFactory()
                 .createCompressorInputStream(CompressorStreamFactory.getLZ4Framed(),
                                              new FileInputStream(getFile("bla.tar.lz4")),
                                              true);
            FileInputStream e = new FileInputStream(getFile("bla.tar"))) {
            byte[] expected = IOUtils.toByteArray(e);
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readDoubledBlaLz4ViaFactoryWithDecompressConcatenatedTrue
    public void readDoubledBlaLz4ViaFactoryWithDecompressConcatenatedTrue() throws Exception {
        readDoubledBlaLz4(new StreamWrapper() {
                @Override
                public InputStream wrap(InputStream in) throws Exception {
                    return new CompressorStreamFactory()
                        .createCompressorInputStream(CompressorStreamFactory.getLZ4Framed(), in, true);
                }
            }, true);
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readDoubledBlaLz4ViaFactoryWithDecompressConcatenatedFalse
    public void readDoubledBlaLz4ViaFactoryWithDecompressConcatenatedFalse() throws Exception {
        readDoubledBlaLz4(new StreamWrapper() {
                @Override
                public InputStream wrap(InputStream in) throws Exception {
                    return new CompressorStreamFactory()
                        .createCompressorInputStream(CompressorStreamFactory.getLZ4Framed(), in, false);
                }
            }, false);
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readDoubledBlaLz4ViaFactoryWithoutExplicitDecompressConcatenated
    public void readDoubledBlaLz4ViaFactoryWithoutExplicitDecompressConcatenated() throws Exception {
        readDoubledBlaLz4(new StreamWrapper() {
                @Override
                public InputStream wrap(InputStream in) throws Exception {
                    return new CompressorStreamFactory()
                        .createCompressorInputStream(CompressorStreamFactory.getLZ4Framed(), in);
                }
            }, false);
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readBlaDumpLz4
    public void readBlaDumpLz4() throws IOException {
        try (InputStream a = new FramedLZ4CompressorInputStream(new FileInputStream(getFile("bla.dump.lz4")));
            FileInputStream e = new FileInputStream(getFile("bla.dump"))) {
            byte[] expected = IOUtils.toByteArray(e);
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsNonLZ4Stream
    public void rejectsNonLZ4Stream() throws IOException {
        try (InputStream a = new FramedLZ4CompressorInputStream(new FileInputStream(getFile("bla.tar")))) {
             fail("expected exception");
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsFileWithoutFrameDescriptor
    public void rejectsFileWithoutFrameDescriptor() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("frame flags"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsFileWithoutBlockSizeByte
    public void rejectsFileWithoutBlockSizeByte() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x64, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("BD byte"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsFileWithWrongVersion
    public void rejectsFileWithWrongVersion() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x24, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("version"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsFileWithInsufficientContentSize
    public void rejectsFileWithInsufficientContentSize() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x6C, 
            0x70, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("content size"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsFileWithoutHeaderChecksum
    public void rejectsFileWithoutHeaderChecksum() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x64, 
            0x70, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("header checksum"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsFileWithBadHeaderChecksum
    public void rejectsFileWithBadHeaderChecksum() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x64, 
            0x70, 
            0,
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("header checksum mismatch"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readsUncompressedBlocks
    public void readsUncompressedBlocks() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
        };
        try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(new byte[] {
                    'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!'
                }, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::readsUncompressedBlocksUsingSingleByteRead
    public void readsUncompressedBlocksUsingSingleByteRead() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
        };
        try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
            int h = a.read();
            assertEquals('H', h);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsBlocksWithoutChecksum
    public void rejectsBlocksWithoutChecksum() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x70, 
            0x70, 
            114, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("block checksum"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsStreamsWithoutContentChecksum
    public void rejectsStreamsWithoutContentChecksum() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x64, 
            0x70, 
            (byte) 185, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("content checksum"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsStreamsWithBadContentChecksum
    public void rejectsStreamsWithBadContentChecksum() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x64, 
            0x70, 
            (byte) 185, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            1, 2, 3, 4,
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input))) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("content checksum mismatch"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::skipsOverSkippableFrames
    public void skipsOverSkippableFrames() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x5f, 0x2a, 0x4d, 0x18, 
            2, 0, 0, 0, 
            1, 2, 
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            1, 0, 0, (byte) 0x80, 
            '!', 
            0, 0, 0, 0, 
        };
        try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(new byte[] {
                    'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', '!'
                }, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::skipsOverTrailingSkippableFrames
    public void skipsOverTrailingSkippableFrames() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x51, 0x2a, 0x4d, 0x18, 
            2, 0, 0, 0, 
            1, 2, 
        };
        try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(new byte[] {
                    'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!'
                }, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsSkippableFrameFollowedByJunk
    public void rejectsSkippableFrameFollowedByJunk() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x50, 0x2a, 0x4d, 0x18, 
            2, 0, 0, 0, 
            1, 2, 
            1, 0x22, 0x4d, 0x18, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("garbage"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsSkippableFrameFollowedByTooFewBytes
    public void rejectsSkippableFrameFollowedByTooFewBytes() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x52, 0x2a, 0x4d, 0x18, 
            2, 0, 0, 0, 
            1, 2, 
            4, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("garbage"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsSkippableFrameWithPrematureEnd
    public void rejectsSkippableFrameWithPrematureEnd() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x50, 0x2a, 0x4d, 0x18, 
            2, 0, 0, 0, 
            1, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("Premature end of stream while skipping frame"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsSkippableFrameWithPrematureEndInLengthBytes
    public void rejectsSkippableFrameWithPrematureEndInLengthBytes() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x55, 0x2a, 0x4d, 0x18, 
            2, 0, 0, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("premature end of data"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsSkippableFrameWithBadSignatureTrailer
    public void rejectsSkippableFrameWithBadSignatureTrailer() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x51, 0x2a, 0x4d, 0x17, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("garbage"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsSkippableFrameWithBadSignaturePrefix
    public void rejectsSkippableFrameWithBadSignaturePrefix() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x60, 0x2a, 0x4d, 0x18, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("garbage"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStreamTest::rejectsTrailingBytesAfterValidFrame
    public void rejectsTrailingBytesAfterValidFrame() throws IOException {
        byte[] input = new byte[] {
            4, 0x22, 0x4d, 0x18, 
            0x60, 
            0x70, 
            115, 
            13, 0, 0, (byte) 0x80, 
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', 
            0, 0, 0, 0, 
            0x56, 0x2a, 0x4d, 
        };
        try {
            try (InputStream a = new FramedLZ4CompressorInputStream(new ByteArrayInputStream(input), true)) {
                IOUtils.toByteArray(a);
                fail("expected exception");
            }
        } catch (IOException ex) {
            assertThat(ex.getMessage(), containsString("garbage"));
        }
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorRoundtripTest::blaTarRoundtrip
    public void blaTarRoundtrip() throws IOException {
        roundTripTest("bla.tar");
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorRoundtripTest::gzippedLoremIpsumRoundtrip
    public void gzippedLoremIpsumRoundtrip() throws IOException {
        roundTripTest("lorem-ipsum.txt.gz");
    }

// org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorRoundtripTest::biggerFileRoundtrip
    public void biggerFileRoundtrip() throws IOException {
        roundTripTest("COMPRESS-256.7z");
    }

// org.apache.commons.compress.utils.ChecksumCalculatingInputStreamTest::testSkipReturningZero
    public void testSkipReturningZero() throws IOException {

        Adler32 adler32 = new Adler32();
        byte[] byteArray = new byte[0];
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(adler32, byteArrayInputStream);
        long skipResult = checksumCalculatingInputStream.skip(60L);

        assertEquals(0L, skipResult);

        assertEquals(1L, checksumCalculatingInputStream.getValue());

    }

// org.apache.commons.compress.utils.ChecksumCalculatingInputStreamTest::testSkipReturningPositive
    public void testSkipReturningPositive() throws IOException {

        Adler32 adler32 = new Adler32();
        byte[] byteArray = new byte[6];
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(adler32, byteArrayInputStream);
        long skipResult = checksumCalculatingInputStream.skip((byte)0);

        assertEquals(1L, skipResult);

        assertEquals(65537L, checksumCalculatingInputStream.getValue());

    }

// org.apache.commons.compress.utils.ChecksumCalculatingInputStreamTest::testReadTakingNoArguments
    public void testReadTakingNoArguments() throws IOException {

        Adler32 adler32 = new Adler32();
        byte[] byteArray = new byte[6];
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(adler32, byteArrayInputStream);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(checksumCalculatingInputStream);
        int inputStreamReadResult = bufferedInputStream.read(byteArray, 0, 1);
        int checkSumCalculationReadResult = checksumCalculatingInputStream.read();

        assertFalse(checkSumCalculationReadResult == inputStreamReadResult);
        assertEquals((-1), checkSumCalculationReadResult);

        assertEquals(0, byteArrayInputStream.available());

        assertEquals(393217L, checksumCalculatingInputStream.getValue());

    }

// org.apache.commons.compress.utils.ChecksumCalculatingInputStreamTest::testReadTakingByteArray
    public void testReadTakingByteArray() throws IOException {

        Adler32 adler32 = new Adler32();
        byte[] byteArray = new byte[6];
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(adler32, byteArrayInputStream);
        int readResult = checksumCalculatingInputStream.read(byteArray);

        assertEquals(6, readResult);

        assertEquals(0, byteArrayInputStream.available());
        assertEquals(393217L, checksumCalculatingInputStream.getValue());

    }

// org.apache.commons.compress.utils.ChecksumCalculatingInputStreamTest::testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionOne
    public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionOne() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(null,null);

    }

// org.apache.commons.compress.utils.ChecksumCalculatingInputStreamTest::testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionTwo
    public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionTwo() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(null,new ByteArrayInputStream(new byte[1]));

    }

// org.apache.commons.compress.utils.ChecksumCalculatingInputStreamTest::testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionThree
    public void testClassInstantiationWithParameterBeingNullThrowsNullPointerExceptionThree() {

        ChecksumCalculatingInputStream checksumCalculatingInputStream = new ChecksumCalculatingInputStream(new CRC32(),null);

    }
