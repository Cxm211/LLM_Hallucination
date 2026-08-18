// buggy code
    public long readBits(final int count) throws IOException {
        if (count < 0 || count > MAXIMUM_CACHE_SIZE) {
            throw new IllegalArgumentException("count must not be negative or greater than " + MAXIMUM_CACHE_SIZE);
        }
        while (bitsCachedSize < count) {
            final long nextByte = in.read();
            if (nextByte < 0) {
                return nextByte;
            }
            if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                bitsCached |= (nextByte << bitsCachedSize);
            } else {
                bitsCached <<= 8;
                bitsCached |= nextByte;
            }
            bitsCachedSize += 8;
        }
            // bitsCachedSize >= 57 and left-shifting it 8 bits would cause an overflow
        
        final long bitsOut;
            if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                bitsOut = (bitsCached & MASKS[count]);
                bitsCached >>>= count;
            } else {
                bitsOut = (bitsCached >> (bitsCachedSize - count)) & MASKS[count];
            }
            bitsCachedSize -= count;
        return bitsOut;
    }

// relevant test
// org.apache.commons.compress.archivers.zip.BinaryTreeTest::testDecode
    public void testDecode() throws IOException {
        final InputStream in = new ByteArrayInputStream(new byte[] { 0x02, 0x42, 0x01, 0x13 });
        
        final BinaryTree tree = BinaryTree.decode(in, 8);
        
        assertNotNull(tree);
        
        final BitStream stream = new BitStream(new ByteArrayInputStream(new byte[] { (byte) 0x8D, (byte) 0xC5, (byte) 0x11, 0x00 }));
        assertEquals(0, tree.read(stream));
        assertEquals(1, tree.read(stream));
        assertEquals(2, tree.read(stream));
        assertEquals(3, tree.read(stream));
        assertEquals(4, tree.read(stream));
        assertEquals(5, tree.read(stream));
        assertEquals(6, tree.read(stream));
        assertEquals(7, tree.read(stream));
    }

// org.apache.commons.compress.archivers.zip.BitStreamTest::testEmptyStream
    public void testEmptyStream() throws Exception {
        final BitStream stream = new BitStream(new ByteArrayInputStream(new byte[0]));
        assertEquals("next bit", -1, stream.nextBit());
        assertEquals("next bit", -1, stream.nextBit());
        assertEquals("next bit", -1, stream.nextBit());
        stream.close();
    }

// org.apache.commons.compress.archivers.zip.BitStreamTest::testStream
    public void testStream() throws Exception {
        final BitStream stream = new BitStream(new ByteArrayInputStream(new byte[] { (byte) 0xEA, 0x03 }));

        assertEquals("bit 0", 0, stream.nextBit());
        assertEquals("bit 1", 1, stream.nextBit());
        assertEquals("bit 2", 0, stream.nextBit());
        assertEquals("bit 3", 1, stream.nextBit());
        assertEquals("bit 4", 0, stream.nextBit());
        assertEquals("bit 5", 1, stream.nextBit());
        assertEquals("bit 6", 1, stream.nextBit());
        assertEquals("bit 7", 1, stream.nextBit());

        assertEquals("bit 8", 1, stream.nextBit());
        assertEquals("bit 9", 1, stream.nextBit());
        assertEquals("bit 10", 0, stream.nextBit());
        assertEquals("bit 11", 0, stream.nextBit());
        assertEquals("bit 12", 0, stream.nextBit());
        assertEquals("bit 13", 0, stream.nextBit());
        assertEquals("bit 14", 0, stream.nextBit());
        assertEquals("bit 15", 0, stream.nextBit());
        
        assertEquals("next bit", -1, stream.nextBit());
        stream.close();
    }

// org.apache.commons.compress.archivers.zip.BitStreamTest::testNextByteFromEmptyStream
    public void testNextByteFromEmptyStream() throws Exception {
        final BitStream stream = new BitStream(new ByteArrayInputStream(new byte[0]));
        assertEquals("next byte", -1, stream.nextByte());
        assertEquals("next byte", -1, stream.nextByte());
        stream.close();
    }

// org.apache.commons.compress.archivers.zip.BitStreamTest::testReadAlignedBytes
    public void testReadAlignedBytes() throws Exception {
        final BitStream stream = new BitStream(new ByteArrayInputStream(new byte[] { (byte) 0xEA, 0x35 }));
        assertEquals("next byte", 0xEA, stream.nextByte());
        assertEquals("next byte", 0x35, stream.nextByte());
        assertEquals("next byte", -1, stream.nextByte());
        stream.close();
    }

// org.apache.commons.compress.archivers.zip.BitStreamTest::testNextByte
    public void testNextByte() throws Exception {
        final BitStream stream = new BitStream(new ByteArrayInputStream(new byte[] { (byte) 0xEA, 0x35 }));
        assertEquals("bit 0", 0, stream.nextBit());
        assertEquals("bit 1", 1, stream.nextBit());
        assertEquals("bit 2", 0, stream.nextBit());
        assertEquals("bit 3", 1, stream.nextBit());
        
        assertEquals("next byte", 0x5E, stream.nextByte());
        assertEquals("next byte", -1, stream.nextByte()); 
        stream.close();
    }

// org.apache.commons.compress.archivers.zip.ExplodeSupportTest::testArchiveWithImplodeCompression4K2Trees
    public void testArchiveWithImplodeCompression4K2Trees() throws IOException {
        testArchiveWithImplodeCompression("target/test-classes/imploding-4Kdict-2trees.zip", "HEADER.TXT");
    }

// org.apache.commons.compress.archivers.zip.ExplodeSupportTest::testArchiveWithImplodeCompression8K3Trees
    public void testArchiveWithImplodeCompression8K3Trees() throws IOException {
        testArchiveWithImplodeCompression("target/test-classes/imploding-8Kdict-3trees.zip", "LICENSE.TXT");
    }

// org.apache.commons.compress.archivers.zip.ExplodeSupportTest::testTikaTestArchive
    public void testTikaTestArchive() throws IOException {
        testArchiveWithImplodeCompression("target/test-classes/moby-imploded.zip", "README");
    }

// org.apache.commons.compress.archivers.zip.ExplodeSupportTest::testZipStreamWithImplodeCompression4K2Trees
    public void testZipStreamWithImplodeCompression4K2Trees() throws IOException {
        testZipStreamWithImplodeCompression("target/test-classes/imploding-4Kdict-2trees.zip", "HEADER.TXT");
    }

// org.apache.commons.compress.archivers.zip.ExplodeSupportTest::testZipStreamWithImplodeCompression8K3Trees
    public void testZipStreamWithImplodeCompression8K3Trees() throws IOException {
        testZipStreamWithImplodeCompression("target/test-classes/imploding-8Kdict-3trees.zip", "LICENSE.TXT");
    }

// org.apache.commons.compress.archivers.zip.ExplodeSupportTest::testTikaTestStream
    public void testTikaTestStream() throws IOException {
        testZipStreamWithImplodeCompression("target/test-classes/moby-imploded.zip", "README");
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::winzipBackSlashWorkaround
    public void winzipBackSlashWorkaround() throws Exception {
        ZipArchiveInputStream in = null;
        try {
            in = new ZipArchiveInputStream(new FileInputStream(getFile("test-winzip.zip")));
            ZipArchiveEntry zae = in.getNextZipEntry();
            zae = in.getNextZipEntry();
            zae = in.getNextZipEntry();
            assertEquals("\u00e4/", zae.getName());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::properUseOfInflater
    public void properUseOfInflater() throws Exception {
        ZipFile zf = null;
        ZipArchiveInputStream in = null;
        try {
            zf = new ZipFile(getFile("COMPRESS-189.zip"));
            final ZipArchiveEntry zae = zf.getEntry("USD0558682-20080101.ZIP");
            in = new ZipArchiveInputStream(new BufferedInputStream(zf.getInputStream(zae)));
            ZipArchiveEntry innerEntry;
            while ((innerEntry = in.getNextZipEntry()) != null) {
                if (innerEntry.getName().endsWith("XML")) {
                    assertTrue(0 < in.read());
                }
            }
        } finally {
            if (zf != null) {
                zf.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::shouldConsumeArchiveCompletely
    public void shouldConsumeArchiveCompletely() throws Exception {
        final InputStream is = ZipArchiveInputStreamTest.class
            .getResourceAsStream("/archive_with_trailer.zip");
        final ZipArchiveInputStream zip = new ZipArchiveInputStream(is);
        while (zip.getNextZipEntry() != null) {
            
        }
        final byte[] expected = new byte[] {
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', '\n'
        };
        final byte[] actual = new byte[expected.length];
        is.read(actual);
        assertArrayEquals(expected, actual);
        zip.close();
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::shouldReadNestedZip
    public void shouldReadNestedZip() throws IOException {
        ZipArchiveInputStream in = null;
        try {
            in = new ZipArchiveInputStream(new FileInputStream(getFile("COMPRESS-219.zip")));
            extractZipInputStream(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::testUnshrinkEntry
    public void testUnshrinkEntry() throws Exception {
        final ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(getFile("SHRUNK.ZIP")));
        
        ZipArchiveEntry entry = in.getNextZipEntry();
        assertEquals("method", ZipMethod.UNSHRINKING.getCode(), entry.getMethod());
        assertTrue(in.canReadEntryData(entry));
        
        FileInputStream original = new FileInputStream(getFile("test1.xml"));
        try {
            assertArrayEquals(IOUtils.toByteArray(original), IOUtils.toByteArray(in));
        } finally {
            original.close();
        }
        
        entry = in.getNextZipEntry();
        assertEquals("method", ZipMethod.UNSHRINKING.getCode(), entry.getMethod());
        assertTrue(in.canReadEntryData(entry));
        
        original = new FileInputStream(getFile("test2.xml"));
        try {
            assertArrayEquals(IOUtils.toByteArray(original), IOUtils.toByteArray(in));
        } finally {
            original.close();
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::testReadingOfFirstStoredEntry
    public void testReadingOfFirstStoredEntry() throws Exception {

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(getFile("COMPRESS-264.zip")))) {
            final ZipArchiveEntry ze = in.getNextZipEntry();
            assertEquals(5, ze.getSize());
            assertArrayEquals(new byte[] { 'd', 'a', 't', 'a', '\n' },
                    IOUtils.toByteArray(in));
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::testMessageWithCorruptFileName
    public void testMessageWithCorruptFileName() throws Exception {
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(getFile("COMPRESS-351.zip")))) {
            ZipArchiveEntry ze = in.getNextZipEntry();
            while (ze != null) {
                ze = in.getNextZipEntry();
            }
            fail("expected EOFException");
        } catch (final EOFException ex) {
            final String m = ex.getMessage();
            assertTrue(m.startsWith("Truncated ZIP entry: ?2016")); 
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::testUnzipBZip2CompressedEntry
    public void testUnzipBZip2CompressedEntry() throws Exception {

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new FileInputStream(getFile("bzip2-zip.zip")))) {
            final ZipArchiveEntry ze = in.getNextZipEntry();
            assertEquals(42, ze.getSize());
            final byte[] expected = new byte[42];
            Arrays.fill(expected, (byte) 'a');
            assertArrayEquals(expected, IOUtils.toByteArray(in));
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testCDOrder
    public void testCDOrder() throws Exception {
        readOrderTest();
        final ArrayList<ZipArchiveEntry> l = Collections.list(zf.getEntries());
        assertEntryName(l, 0, "AbstractUnicodeExtraField");
        assertEntryName(l, 1, "AsiExtraField");
        assertEntryName(l, 2, "ExtraFieldUtils");
        assertEntryName(l, 3, "FallbackZipEncoding");
        assertEntryName(l, 4, "GeneralPurposeBit");
        assertEntryName(l, 5, "JarMarker");
        assertEntryName(l, 6, "NioZipEncoding");
        assertEntryName(l, 7, "Simple8BitZipEncoding");
        assertEntryName(l, 8, "UnicodeCommentExtraField");
        assertEntryName(l, 9, "UnicodePathExtraField");
        assertEntryName(l, 10, "UnixStat");
        assertEntryName(l, 11, "UnparseableExtraFieldData");
        assertEntryName(l, 12, "UnrecognizedExtraField");
        assertEntryName(l, 13, "ZipArchiveEntry");
        assertEntryName(l, 14, "ZipArchiveInputStream");
        assertEntryName(l, 15, "ZipArchiveOutputStream");
        assertEntryName(l, 16, "ZipEncoding");
        assertEntryName(l, 17, "ZipEncodingHelper");
        assertEntryName(l, 18, "ZipExtraField");
        assertEntryName(l, 19, "ZipUtil");
        assertEntryName(l, 20, "ZipLong");
        assertEntryName(l, 21, "ZipShort");
        assertEntryName(l, 22, "ZipFile");
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testPhysicalOrder
    public void testPhysicalOrder() throws Exception {
        readOrderTest();
        final ArrayList<ZipArchiveEntry> l = Collections.list(zf.getEntriesInPhysicalOrder());
        assertEntryName(l, 0, "AbstractUnicodeExtraField");
        assertEntryName(l, 1, "AsiExtraField");
        assertEntryName(l, 2, "ExtraFieldUtils");
        assertEntryName(l, 3, "FallbackZipEncoding");
        assertEntryName(l, 4, "GeneralPurposeBit");
        assertEntryName(l, 5, "JarMarker");
        assertEntryName(l, 6, "NioZipEncoding");
        assertEntryName(l, 7, "Simple8BitZipEncoding");
        assertEntryName(l, 8, "UnicodeCommentExtraField");
        assertEntryName(l, 9, "UnicodePathExtraField");
        assertEntryName(l, 10, "UnixStat");
        assertEntryName(l, 11, "UnparseableExtraFieldData");
        assertEntryName(l, 12, "UnrecognizedExtraField");
        assertEntryName(l, 13, "ZipArchiveEntry");
        assertEntryName(l, 14, "ZipArchiveInputStream");
        assertEntryName(l, 15, "ZipArchiveOutputStream");
        assertEntryName(l, 16, "ZipEncoding");
        assertEntryName(l, 17, "ZipEncodingHelper");
        assertEntryName(l, 18, "ZipExtraField");
        assertEntryName(l, 19, "ZipFile");
        assertEntryName(l, 20, "ZipLong");
        assertEntryName(l, 21, "ZipShort");
        assertEntryName(l, 22, "ZipUtil");
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testDoubleClose
    public void testDoubleClose() throws Exception {
        readOrderTest();
        zf.close();
        try {
            zf.close();
        } catch (final Exception ex) {
            fail("Caught exception of second close");
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testReadingOfStoredEntry
    public void testReadingOfStoredEntry() throws Exception {
        final File f = File.createTempFile("commons-compress-zipfiletest", ".zip");
        f.deleteOnExit();
        OutputStream o = null;
        InputStream i = null;
        try {
            o = new FileOutputStream(f);
            final ZipArchiveOutputStream zo = new ZipArchiveOutputStream(o);
            ZipArchiveEntry ze = new ZipArchiveEntry("foo");
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(4);
            ze.setCrc(0xb63cfbcdl);
            zo.putArchiveEntry(ze);
            zo.write(new byte[] { 1, 2, 3, 4 });
            zo.closeArchiveEntry();
            zo.close();
            o.close();
            o  = null;

            zf = new ZipFile(f);
            ze = zf.getEntry("foo");
            assertNotNull(ze);
            i = zf.getInputStream(ze);
            final byte[] b = new byte[4];
            assertEquals(4, i.read(b));
            assertEquals(-1, i.read());
        } finally {
            if (o != null) {
                o.close();
            }
            if (i != null) {
                i.close();
            }
            f.delete();
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testWinzipBackSlashWorkaround
    public void testWinzipBackSlashWorkaround() throws Exception {
        final File archive = getFile("test-winzip.zip");
        zf = new ZipFile(archive);
        assertNull(zf.getEntry("\u00e4\\\u00fc.txt"));
        assertNotNull(zf.getEntry("\u00e4/\u00fc.txt"));
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testSkipsPK00Prefix
    public void testSkipsPK00Prefix() throws Exception {
        final File archive = getFile("COMPRESS-208.zip");
        zf = new ZipFile(archive);
        assertNotNull(zf.getEntry("test1.xml"));
        assertNotNull(zf.getEntry("test2.xml"));
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testUnixSymlinkSampleFile
    public void testUnixSymlinkSampleFile() throws Exception {
        final String entryPrefix = "COMPRESS-214_unix_symlinks/";
        final TreeMap<String, String> expectedVals = new TreeMap<>();

        
        expectedVals.put(entryPrefix + "link1", "../COMPRESS-214_unix_symlinks/./a/b/c/../../../\uF999");
        expectedVals.put(entryPrefix + "link2", "../COMPRESS-214_unix_symlinks/./a/b/c/../../../g");
        expectedVals.put(entryPrefix + "link3", "../COMPRESS-214_unix_symlinks/././a/b/c/../../../\u76F4\u6A39");
        expectedVals.put(entryPrefix + "link4", "\u82B1\u5B50/\u745B\u5B50");
        expectedVals.put(entryPrefix + "\uF999", "./\u82B1\u5B50/\u745B\u5B50/\u5897\u8C37/\uF999");
        expectedVals.put(entryPrefix + "g", "./a/b/c/d/e/f/g");
        expectedVals.put(entryPrefix + "\u76F4\u6A39", "./g");

        
        
        expectedVals.put(entryPrefix + "link5", "../COMPRESS-214_unix_symlinks/././a/b");
        expectedVals.put(entryPrefix + "link6", "../COMPRESS-214_unix_symlinks/././a/b/");

        
        

        final File archive = getFile("COMPRESS-214_unix_symlinks.zip");

        zf = new ZipFile(archive);
        final Enumeration<ZipArchiveEntry> en = zf.getEntries();
        while (en.hasMoreElements()) {
            final ZipArchiveEntry zae = en.nextElement();
            final String link = zf.getUnixSymlink(zae);
            if (zae.isUnixSymlink()) {
                final String name = zae.getName();
                final String expected = expectedVals.get(name);
                assertEquals(expected, link);
            } else {
                
                assertNull(link);
            }
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testDuplicateEntry
    public void testDuplicateEntry() throws Exception {
        final File archive = getFile("COMPRESS-227.zip");
        zf = new ZipFile(archive);

        final ZipArchiveEntry ze = zf.getEntry("test1.txt");
        assertNotNull(ze);
        assertNotNull(zf.getInputStream(ze));

        int numberOfEntries = 0;
        for (final ZipArchiveEntry entry : zf.getEntries("test1.txt")) {
            numberOfEntries++;
            assertNotNull(zf.getInputStream(entry));
        }
        assertEquals(2, numberOfEntries);
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testExcessDataInZip64ExtraField
    public void testExcessDataInZip64ExtraField() throws Exception {
        final File archive = getFile("COMPRESS-228.zip");
        zf = new ZipFile(archive);
        

        final ZipArchiveEntry ze = zf.getEntry("src/main/java/org/apache/commons/compress/archivers/zip/ZipFile.java");
        assertEquals(26101, ze.getSize());
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testUnshrinking
    public void testUnshrinking() throws Exception {
        zf = new ZipFile(getFile("SHRUNK.ZIP"));
        ZipArchiveEntry test = zf.getEntry("TEST1.XML");
        FileInputStream original = new FileInputStream(getFile("test1.xml"));
        try {
            assertArrayEquals(IOUtils.toByteArray(original),
                              IOUtils.toByteArray(zf.getInputStream(test)));
        } finally {
            original.close();
        }
        test = zf.getEntry("TEST2.XML");
        original = new FileInputStream(getFile("test2.xml"));
        try {
            assertArrayEquals(IOUtils.toByteArray(original),
                              IOUtils.toByteArray(zf.getInputStream(test)));
        } finally {
            original.close();
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testReadingOfFirstStoredEntry
    public void testReadingOfFirstStoredEntry() throws Exception {
        final File archive = getFile("COMPRESS-264.zip");
        zf = new ZipFile(archive);
        final ZipArchiveEntry ze = zf.getEntry("test.txt");
        assertEquals(5, ze.getSize());
        assertArrayEquals(new byte[] {'d', 'a', 't', 'a', '\n'},
                          IOUtils.toByteArray(zf.getInputStream(ze)));
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testUnzipBZip2CompressedEntry
    public void testUnzipBZip2CompressedEntry() throws Exception {
        final File archive = getFile("bzip2-zip.zip");
        zf = new ZipFile(archive);
        final ZipArchiveEntry ze = zf.getEntry("lots-of-as");
        assertEquals(42, ze.getSize());
        final byte[] expected = new byte[42];
        Arrays.fill(expected , (byte)'a');
        assertArrayEquals(expected, IOUtils.toByteArray(zf.getInputStream(ze)));
    }

// org.apache.commons.compress.compressors.ZTestCase::testZUnarchive
    public void testZUnarchive() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            @Override
            public CompressorInputStream wrap(final InputStream is) throws IOException {
                return new ZCompressorInputStream(is);
            }
        });
    }

// org.apache.commons.compress.compressors.ZTestCase::testZUnarchiveViaFactory
    public void testZUnarchiveViaFactory() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            @Override
            public CompressorInputStream wrap(final InputStream is) throws Exception {
                return new CompressorStreamFactory()
                    .createCompressorInputStream(CompressorStreamFactory.Z, is);
            }
        });
    }

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

// org.apache.commons.compress.utils.BitInputStreamTest::shouldNotAllowReadingOfANegativeAmountOfBits
    public void shouldNotAllowReadingOfANegativeAmountOfBits() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN);
        bis.readBits(-1);
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::shouldNotAllowReadingOfMoreThan63BitsAtATime
    public void shouldNotAllowReadingOfMoreThan63BitsAtATime() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN);
        bis.readBits(64);
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testReading24BitsInLittleEndian
    public void testReading24BitsInLittleEndian() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x000140f8, bis.readBits(24));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testReading24BitsInBigEndian
    public void testReading24BitsInBigEndian() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.BIG_ENDIAN);
        assertEquals(0x00f84001, bis.readBits(24));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testReading17BitsInLittleEndian
    public void testReading17BitsInLittleEndian() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x000140f8, bis.readBits(17));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testReading17BitsInBigEndian
    public void testReading17BitsInBigEndian() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.BIG_ENDIAN);
        
        assertEquals(0x0001f080, bis.readBits(17));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testReading30BitsInLittleEndian
    public void testReading30BitsInLittleEndian() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x2f0140f8, bis.readBits(30));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testReading30BitsInBigEndian
    public void testReading30BitsInBigEndian() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.BIG_ENDIAN);
        
        assertEquals(0x3e10004b, bis.readBits(30));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testReading31BitsInLittleEndian
    public void testReading31BitsInLittleEndian() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x2f0140f8, bis.readBits(31));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testReading31BitsInBigEndian
    public void testReading31BitsInBigEndian() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.BIG_ENDIAN);
        
        assertEquals(0x7c200097, bis.readBits(31));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testClearBitCache
    public void testClearBitCache() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x08, bis.readBits(4));
        bis.clearBitCache();
        assertEquals(0, bis.readBits(1));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::testEOF
    public void testEOF() throws IOException {
        final BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x2f0140f8, bis.readBits(30));
        assertEquals(-1, bis.readBits(3));
        bis.close();
    }

// org.apache.commons.compress.utils.BitInputStreamTest::littleEndianWithOverflow
    public void littleEndianWithOverflow() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {
                87, 
                45, 
                66, 
                15, 
                90, 
                29, 
                88, 
                61, 
                33, 
                74  
            });
        BitInputStream bin = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        assertEquals(23, 
                     bin.readBits(5));
        assertEquals(714595605644185962l, 
                     bin.readBits(63));
        assertEquals(1186, 
                     bin.readBits(12));
        assertEquals(-1 , bin.readBits(1));
    }

// org.apache.commons.compress.utils.BitInputStreamTest::bigEndianWithOverflow
    public void bigEndianWithOverflow() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {
                87, 
                45, 
                66, 
                15, 
                90, 
                29, 
                88, 
                61, 
                33, 
                74  
            });
        BitInputStream bin = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        assertEquals(10, 
                     bin.readBits(5));
        assertEquals(8274274654740644818l, 
                     bin.readBits(63));
        assertEquals(330, 
                     bin.readBits(12));
        assertEquals(-1 , bin.readBits(1));
    }
