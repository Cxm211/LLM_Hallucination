// buggy code
    public ZipArchiveEntry getNextZipEntry() throws IOException {
        boolean firstEntry = true;
        if (closed || hitCentralDirectory) {
            return null;
        }
        if (current != null) {
            closeEntry();
            firstEntry = false;
        }

        try {
            if (firstEntry) {
                // split archives have a special signature before the
                // first local file header - look for it and fail with
                // the appropriate error message if this is a split
                // archive.
                readFirstLocalFileHeader(LFH_BUF);
            } else {
                readFully(LFH_BUF);
            }
        } catch (final EOFException e) {
            return null;
        }

        final ZipLong sig = new ZipLong(LFH_BUF);
        if (sig.equals(ZipLong.CFH_SIG) || sig.equals(ZipLong.AED_SIG)) {
            hitCentralDirectory = true;
            skipRemainderOfArchive();
        }
        if (!sig.equals(ZipLong.LFH_SIG)) {
            return null;
        }

        int off = WORD;
        current = new CurrentEntry();

        final int versionMadeBy = ZipShort.getValue(LFH_BUF, off);
        off += SHORT;
        current.entry.setPlatform((versionMadeBy >> ZipFile.BYTE_SHIFT) & ZipFile.NIBLET_MASK);

        final GeneralPurposeBit gpFlag = GeneralPurposeBit.parse(LFH_BUF, off);
        final boolean hasUTF8Flag = gpFlag.usesUTF8ForNames();
        final ZipEncoding entryEncoding = hasUTF8Flag ? ZipEncodingHelper.UTF8_ZIP_ENCODING : zipEncoding;
        current.hasDataDescriptor = gpFlag.usesDataDescriptor();
        current.entry.setGeneralPurposeBit(gpFlag);

        off += SHORT;

        current.entry.setMethod(ZipShort.getValue(LFH_BUF, off));
        off += SHORT;

        final long time = ZipUtil.dosToJavaTime(ZipLong.getValue(LFH_BUF, off));
        current.entry.setTime(time);
        off += WORD;

        ZipLong size = null, cSize = null;
        if (!current.hasDataDescriptor) {
            current.entry.setCrc(ZipLong.getValue(LFH_BUF, off));
            off += WORD;

            cSize = new ZipLong(LFH_BUF, off);
            off += WORD;

            size = new ZipLong(LFH_BUF, off);
            off += WORD;
        } else {
            off += 3 * WORD;
        }

        final int fileNameLen = ZipShort.getValue(LFH_BUF, off);

        off += SHORT;

        final int extraLen = ZipShort.getValue(LFH_BUF, off);
        off += SHORT;

        final byte[] fileName = new byte[fileNameLen];
        readFully(fileName);
        current.entry.setName(entryEncoding.decode(fileName), fileName);

        final byte[] extraData = new byte[extraLen];
        readFully(extraData);
        current.entry.setExtra(extraData);

        if (!hasUTF8Flag && useUnicodeExtraFields) {
            ZipUtil.setNameAndCommentFromExtraFields(current.entry, fileName, null);
        }

        processZip64Extra(size, cSize);

        if (current.entry.getCompressedSize() != ArchiveEntry.SIZE_UNKNOWN) {
            if (current.entry.getMethod() == ZipMethod.UNSHRINKING.getCode()) {
                current.in = new UnshrinkingInputStream(new BoundedInputStream(in, current.entry.getCompressedSize()));
            } else if (current.entry.getMethod() == ZipMethod.IMPLODING.getCode()) {
                current.in = new ExplodingInputStream(
                        current.entry.getGeneralPurposeBit().getSlidingDictionarySize(),
                        current.entry.getGeneralPurposeBit().getNumberOfShannonFanoTrees(),
                        new BoundedInputStream(in, current.entry.getCompressedSize()));
            } else if (current.entry.getMethod() == ZipMethod.BZIP2.getCode()) {
                current.in = new BZip2CompressorInputStream(new BoundedInputStream(in, current.entry.getCompressedSize()));
            }
        }
        
        entriesRead++;
        return current.entry;
    }

// relevant test
// org.apache.commons.compress.compressors.ZTestCase::testZUnarchiveViaAutoDetection
    public void testZUnarchiveViaAutoDetection() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            @Override
            public CompressorInputStream wrap(final InputStream is) throws Exception {
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

// org.apache.commons.compress.compressors.pack200.Pack200UtilsTest::testNormalize
    public void testNormalize() throws Throwable {
        final File input = getFile("bla.jar");
        final File[] output = createTempDirAndFile();
        try {
            Pack200Utils.normalize(input, output[1],
                                   new HashMap<String, String>());
            try (FileInputStream is = new FileInputStream(output[1])) {
                final ArchiveInputStream in = new ArchiveStreamFactory()
                        .createArchiveInputStream("jar", is);

                ArchiveEntry entry = in.getNextEntry();
                while (entry != null) {
                    final File archiveEntry = new File(dir, entry.getName());
                    archiveEntry.getParentFile().mkdirs();
                    if (entry.isDirectory()) {
                        archiveEntry.mkdir();
                        entry = in.getNextEntry();
                        continue;
                    }
                    final OutputStream out = new FileOutputStream(archiveEntry);
                    IOUtils.copy(in, out);
                    out.close();
                    entry = in.getNextEntry();
                }

                in.close();
            }
        } finally {
            output[1].delete();
            output[0].delete();
        }
    }

// org.apache.commons.compress.compressors.pack200.Pack200UtilsTest::testNormalizeInPlace
    public void testNormalizeInPlace() throws Throwable {
        final File input = getFile("bla.jar");
        final File[] output = createTempDirAndFile();
        try {
            FileInputStream is = new FileInputStream(input);
            OutputStream os = null;
            try {
                os = new FileOutputStream(output[1]);
                IOUtils.copy(is, os);
            } finally {
                is.close();
                if (os != null) {
                    os.close();
                }
            }

            Pack200Utils.normalize(output[1]);
            is = new FileInputStream(output[1]);
            try {
                final ArchiveInputStream in = new ArchiveStreamFactory()
                    .createArchiveInputStream("jar", is);

                ArchiveEntry entry = in.getNextEntry();
                while (entry != null) {
                    final File archiveEntry = new File(dir, entry.getName());
                    archiveEntry.getParentFile().mkdirs();
                    if (entry.isDirectory()) {
                        archiveEntry.mkdir();
                        entry = in.getNextEntry();
                        continue;
                    }
                    final OutputStream out = new FileOutputStream(archiveEntry);
                    IOUtils.copy(in, out);
                    out.close();
                    entry = in.getNextEntry();
                }

                in.close();
            } finally {
                is.close();
            }
        } finally {
            output[1].delete();
            output[0].delete();
        }
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testMatches
    public void testMatches() throws IOException {
        assertFalse(FramedSnappyCompressorInputStream.matches(new byte[10], 10));
        final byte[] b = new byte[12];
        final File input = getFile("bla.tar.sz");
        try (FileInputStream in = new FileInputStream(input)) {
            IOUtils.readFully(in, b);
        }
        assertFalse(FramedSnappyCompressorInputStream.matches(b, 9));
        assertTrue(FramedSnappyCompressorInputStream.matches(b, 10));
        assertTrue(FramedSnappyCompressorInputStream.matches(b, 12));
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testLoremIpsum
    public void testLoremIpsum() throws Exception {
        final File outputSz = new File(dir, "lorem-ipsum.1");
        final File outputGz = new File(dir, "lorem-ipsum.2");
        try (FileInputStream isSz = new FileInputStream(getFile("lorem-ipsum.txt.sz"))) {
            InputStream in = new FramedSnappyCompressorInputStream(isSz);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(outputSz);
                IOUtils.copy(in, out);
            } finally {
                if (out != null) {
                    out.close();
                }
                in.close();
            }
            try (FileInputStream isGz = new FileInputStream(getFile("lorem-ipsum.txt.gz"))) {
                in = new GzipCompressorInputStream(isGz);
                try {
                    out = new FileOutputStream(outputGz);
                    IOUtils.copy(in, out);
                } finally {
                    if (out != null) {
                        out.close();
                    }
                    in.close();
                }
            }
        }

        try (FileInputStream sz = new FileInputStream(outputSz)) {
            try (FileInputStream gz = new FileInputStream(outputGz)) {
                assertArrayEquals(IOUtils.toByteArray(sz),
                        IOUtils.toByteArray(gz));
            }
        }
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testRemainingChunkTypes
    public void testRemainingChunkTypes() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (FileInputStream isSz = new FileInputStream(getFile("mixed.txt.sz"))) {
            final FramedSnappyCompressorInputStream in = new FramedSnappyCompressorInputStream(isSz);
            IOUtils.copy(in, out);
            out.close();
        }

        assertArrayEquals(new byte[] { '1', '2', '3', '4',
                                       '5', '6', '7', '8', '9',
                                       '5', '6', '7', '8', '9',
                                       '5', '6', '7', '8', '9',
                                       '5', '6', '7', '8', '9',
                                       '5', '6', '7', '8', '9', 10,
                                       '1', '2', '3', '4',
                                       '1', '2', '3', '4',
            }, out.toByteArray());
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testAvailable
    public void testAvailable() throws Exception {
        try (FileInputStream isSz = new FileInputStream(getFile("mixed.txt.sz"))) {
            final FramedSnappyCompressorInputStream in = new FramedSnappyCompressorInputStream(isSz);
            assertEquals(0, in.available()); 
            assertEquals('1', in.read());
            assertEquals(3, in.available()); 
            assertEquals(3, in.read(new byte[5], 0, 3));
            assertEquals('5', in.read());
            assertEquals(4, in.available()); 
            assertEquals(4, in.read(new byte[5], 0, 4));
            assertEquals('5', in.read());
            assertEquals(19, in.available()); 
            in.close();
        }
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testUnskippableChunk
    public void testUnskippableChunk() {
        final byte[] input = new byte[] {
            (byte) 0xff, 6, 0, 0, 's', 'N', 'a', 'P', 'p', 'Y',
            2, 2, 0, 0, 1, 1
        };
        try {
            final FramedSnappyCompressorInputStream in =
                new FramedSnappyCompressorInputStream(new ByteArrayInputStream(input));
            in.read();
            fail("expected an exception");
            in.close();
        } catch (final IOException ex) {
            assertTrue(ex.getMessage().contains("unskippable chunk"));
        }
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testChecksumUnmasking
    public void testChecksumUnmasking() {
        testChecksumUnmasking(0xc757l);
        testChecksumUnmasking(0xffffc757l);
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::readIWAFile
    public void readIWAFile() throws Exception {
        try (ZipFile zip = new ZipFile(getFile("testNumbersNew.numbers"))) {
            try (InputStream is = zip.getInputStream(zip.getEntry("Index/Document.iwa"))) {
                final FramedSnappyCompressorInputStream in =
                        new FramedSnappyCompressorInputStream(is, FramedSnappyDialect.IWORK_ARCHIVE);
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(new File(dir, "snappyIWATest.raw"));
                    IOUtils.copy(in, out);
                } finally {
                    if (out != null) {
                        out.close();
                    }
                    in.close();
                }
            }
        }
    }
