// buggy code
    private InputStream getCurrentStream() throws IOException {
        if (deferredBlockStreams.isEmpty()) {
            throw new IllegalStateException("No current 7z entry (call getNextEntry() first).");
        }
        
        while (deferredBlockStreams.size() > 1) {
            // In solid compression mode we need to decompress all leading folder'
            // streams to get access to an entry. We defer this until really needed
            // so that entire blocks can be skipped without wasting time for decompression.
            final InputStream stream = deferredBlockStreams.remove(0);
            IOUtils.skip(stream, Long.MAX_VALUE);
            stream.close();
        }

        return deferredBlockStreams.get(0);
    }

// relevant test
// org.apache.commons.compress.ArchiveReadTest::testArchive
    public void testArchive() throws Exception{
        @SuppressWarnings("unchecked") 
        final
        ArrayList<String> expected= (ArrayList<String>) FILELIST.clone();
        try {
           checkArchiveContent(file, expected);
        } catch (final ArchiveException e) {
            fail("Problem checking "+file);
        } catch (final AssertionError e) { 
            fail("Problem checking " + file + " " +e);
        }
    }

// org.apache.commons.compress.DetectArchiverTestCase::testDetectionNotArchive
    public void testDetectionNotArchive() throws IOException {
        try {
            getStreamFor("test.txt");
            fail("Expected ArchiveException");
        } catch (final ArchiveException e) {
            
        }
    }

// org.apache.commons.compress.DetectArchiverTestCase::testCOMPRESS117
    public void testCOMPRESS117() throws Exception {
        final ArchiveInputStream tar = getStreamFor("COMPRESS-117.tar");
        assertNotNull(tar);
        assertTrue(tar instanceof TarArchiveInputStream);
    }

// org.apache.commons.compress.DetectArchiverTestCase::testCOMPRESS335
    public void testCOMPRESS335() throws Exception {
        final ArchiveInputStream tar = getStreamFor("COMPRESS-335.tar");
        assertNotNull(tar);
        assertTrue(tar instanceof TarArchiveInputStream);
    }

// org.apache.commons.compress.DetectArchiverTestCase::testDetection
    public void testDetection() throws Exception {

        final ArchiveInputStream ar = getStreamFor("bla.ar"); 
        assertNotNull(ar);
        assertTrue(ar instanceof ArArchiveInputStream);

        final ArchiveInputStream tar = getStreamFor("bla.tar");
        assertNotNull(tar);
        assertTrue(tar instanceof TarArchiveInputStream);

        final ArchiveInputStream zip = getStreamFor("bla.zip");
        assertNotNull(zip);
        assertTrue(zip instanceof ZipArchiveInputStream);

        final ArchiveInputStream jar = getStreamFor("bla.jar");
        assertNotNull(jar);
        assertTrue(jar instanceof ZipArchiveInputStream);

        final ArchiveInputStream cpio = getStreamFor("bla.cpio");
        assertNotNull(cpio);
        assertTrue(cpio instanceof CpioArchiveInputStream);
        
        final ArchiveInputStream arj = getStreamFor("bla.arj");
        assertNotNull(arj);
        assertTrue(arj instanceof ArjArchiveInputStream);

    }

// org.apache.commons.compress.DetectArchiverTestCase::testEmptyCpioArchive
    public void testEmptyCpioArchive() throws Exception {
        checkEmptyArchive("cpio");
    }

// org.apache.commons.compress.DetectArchiverTestCase::testEmptyJarArchive
    public void testEmptyJarArchive() throws Exception {
        checkEmptyArchive("jar");
    }

// org.apache.commons.compress.DetectArchiverTestCase::testEmptyZipArchive
    public void testEmptyZipArchive() throws Exception {
        checkEmptyArchive("zip");
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::shortTextFilesAreNoTARs
    public void shortTextFilesAreNoTARs() throws Exception {
        try {
            new ArchiveStreamFactory()
                .createArchiveInputStream(new ByteArrayInputStream("This certainly is not a tar archive, really, no kidding".getBytes()));
            fail("created an input stream for a non-archive");
        } catch (final ArchiveException ae) {
            assertTrue(ae.getMessage().startsWith("No Archiver found"));
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::aiffFilesAreNoTARs
    public void aiffFilesAreNoTARs() throws Exception {
    	final FileInputStream fis = new FileInputStream("src/test/resources/testAIFF.aif");
    	try {
            final InputStream is = new BufferedInputStream(fis);
            try {
                new ArchiveStreamFactory().createArchiveInputStream(is);
                fail("created an input stream for a non-archive");
            } catch (final ArchiveException ae) {
                assertTrue(ae.getMessage().startsWith("No Archiver found"));
            } finally {
                is.close();
            }
    	} finally {
            fis.close();
    	}
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testCOMPRESS209
    public void testCOMPRESS209() throws Exception {
    	final FileInputStream fis = new FileInputStream("src/test/resources/testCompress209.doc");
    	try {
            final InputStream bis = new BufferedInputStream(fis);
            try {
                new ArchiveStreamFactory().createArchiveInputStream(bis);
                fail("created an input stream for a non-archive");
            } catch (final ArchiveException ae) {
                assertTrue(ae.getMessage().startsWith("No Archiver found"));
            } finally {
                bis.close();
            }
    	} finally {
            fis.close();
    	}
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::cantRead7zFromStream
    public void cantRead7zFromStream() throws Exception {
        new ArchiveStreamFactory()
            .createArchiveInputStream(ArchiveStreamFactory.SEVEN_Z,
                                      new ByteArrayInputStream(new byte[0]));
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::cantWrite7zToStream
    public void cantWrite7zToStream() throws Exception {
        new ArchiveStreamFactory()
            .createArchiveOutputStream(ArchiveStreamFactory.SEVEN_Z,
                                       new ByteArrayOutputStream());
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::detectsAndThrowsFor7z
    public void detectsAndThrowsFor7z() throws Exception {
    	final FileInputStream fis = new FileInputStream("src/test/resources/bla.7z");
    	try {
            final InputStream bis = new BufferedInputStream(fis);
            try {
                new ArchiveStreamFactory().createArchiveInputStream(bis);
                fail("Expected a StreamingNotSupportedException");
            } catch (final StreamingNotSupportedException ex) {
                assertEquals(ArchiveStreamFactory.SEVEN_Z, ex.getFormat());
            } finally {
                bis.close();
            }
    	} finally {
            fis.close();
    	}
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::skipsPK00Prefix
    public void skipsPK00Prefix() throws Exception {
    	final FileInputStream fis = new FileInputStream("src/test/resources/COMPRESS-208.zip");
    	try {
            final InputStream bis = new BufferedInputStream(fis);
            try {
                final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(bis);
                try {
                    assertTrue(ais instanceof ZipArchiveInputStream);
                } finally {
                    ais.close();
                }
            } finally {
                bis.close();
            }
    	} finally {
            fis.close();
    	}
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testEncodingCtor
    public void testEncodingCtor() {
        ArchiveStreamFactory fac = new ArchiveStreamFactory();
        assertNull(fac.getEntryEncoding());
        fac = new ArchiveStreamFactory(null);
        assertNull(fac.getEntryEncoding());
        fac = new ArchiveStreamFactory("UTF-8");
        assertEquals("UTF-8", fac.getEntryEncoding());
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testEncodingDeprecated
    public void testEncodingDeprecated() {
        ArchiveStreamFactory fac = new ArchiveStreamFactory();
        assertNull(fac.getEntryEncoding());
        fac.setEntryEncoding("UTF-8");
        assertEquals("UTF-8", fac.getEntryEncoding());
        fac.setEntryEncoding("US_ASCII");
        assertEquals("US_ASCII", fac.getEntryEncoding());
        fac = new ArchiveStreamFactory("UTF-8");
        assertEquals("UTF-8", fac.getEntryEncoding());
        try {
            fac.setEntryEncoding("US_ASCII");
            fail("Expected IllegalStateException");
        } catch (final IllegalStateException ise) {
            
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testEncodingInputStreamAutodetect
    public void testEncodingInputStreamAutodetect() throws Exception {
        int failed = 0;
        for(int i = 1; i <= TESTS.length; i++) {
            final TestData test = TESTS[i-1];
            final ArchiveInputStream ais = getInputStreamFor(test.testFile, test.fac);
            final String field = getField(ais,test.fieldName);
            if (!eq(test.expectedEncoding,field)) {
                System.out.println("Failed test " + i + ". expected: " + test.expectedEncoding + " actual: " + field + " type: " + test.type);
                failed++;
            }
        }
        if (failed > 0) {
            fail("Tests failed: " + failed);
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testEncodingInputStream
    public void testEncodingInputStream() throws Exception {
        int failed = 0;
        for(int i = 1; i <= TESTS.length; i++) {
            final TestData test = TESTS[i-1];
            final ArchiveInputStream ais = getInputStreamFor(test.type, test.testFile, test.fac);
            final String field = getField(ais,test.fieldName);
            if (!eq(test.expectedEncoding,field)) {
                System.out.println("Failed test " + i + ". expected: " + test.expectedEncoding + " actual: " + field + " type: " + test.type);
                failed++;
            }
        }
        if (failed > 0) {
            fail("Tests failed: " + failed);
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testEncodingOutputStream
    public void testEncodingOutputStream() throws Exception {
        int failed = 0;
        for(int i = 1; i <= TESTS.length; i++) {
            final TestData test = TESTS[i-1];
            if (test.hasOutputStream) {
                final ArchiveOutputStream ais = getOutputStreamFor(test.type, test.fac);
                final String field = getField(ais, test.fieldName);
                if (!eq(test.expectedEncoding, field)) {
                    System.out.println("Failed test " + i + ". expected: " + test.expectedEncoding + " actual: " + field + " type: " + test.type);
                    failed++;
                }
            }
        }
        if (failed > 0) {
            fail("Tests failed: " + failed);
        }
    }

// org.apache.commons.compress.archivers.DumpTestCase::testDumpUnarchiveAll
    public void testDumpUnarchiveAll() throws Exception {
        unarchiveAll(getFile("bla.dump"));
    }

// org.apache.commons.compress.archivers.DumpTestCase::testCompressedDumpUnarchiveAll
    public void testCompressedDumpUnarchiveAll() throws Exception {
        unarchiveAll(getFile("bla.z.dump"));
    }

// org.apache.commons.compress.archivers.DumpTestCase::testArchiveDetection
    public void testArchiveDetection() throws Exception {
        archiveDetection(getFile("bla.dump"));
    }

// org.apache.commons.compress.archivers.DumpTestCase::testCompressedArchiveDetection
    public void testCompressedArchiveDetection() throws Exception {
        archiveDetection(getFile("bla.z.dump"));
    }

// org.apache.commons.compress.archivers.DumpTestCase::testCheckArchive
    public void testCheckArchive() throws Exception {
        checkDumpArchive(getFile("bla.dump"));
    }

// org.apache.commons.compress.archivers.DumpTestCase::testCheckCompressedArchive
    public void testCheckCompressedArchive() throws Exception {
        checkDumpArchive(getFile("bla.z.dump"));
    }

// org.apache.commons.compress.archivers.LongSymLinkTest::testArchive
    public void testArchive() throws Exception {
        @SuppressWarnings("unchecked") 
        final
        ArrayList<String> expected = (ArrayList<String>) FILELIST.clone();
        final String name = file.getName();
        if ("minotaur.jar".equals(name) || "minotaur-0.jar".equals(name)){
            expected.add("META-INF/");
            expected.add("META-INF/MANIFEST.MF");
        }
        final ArchiveInputStream ais = factory.createArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
        
        if (name.endsWith(".tar")){
            assertTrue(ais instanceof TarArchiveInputStream);
        } else if (name.endsWith(".jar") || name.endsWith(".zip")){
            assertTrue(ais instanceof ZipArchiveInputStream);
        } else if (name.endsWith(".cpio")){
            assertTrue(ais instanceof CpioArchiveInputStream);
            
            for(int i=0; i < expected.size(); i++){
                final String ent = expected.get(i);
                if (ent.endsWith("/")){
                    expected.set(i, ent.substring(0, ent.length()-1));
                }
            }
        } else if (name.endsWith(".ar")){
            assertTrue(ais instanceof ArArchiveInputStream);
            
            expected.clear();
            for (final String ent : FILELIST) {
                if (!ent.endsWith("/")) {// not a directory
                    final int lastSlash = ent.lastIndexOf('/');
                    if (lastSlash >= 0) { 
                        expected.add(ent.substring(lastSlash + 1, ent.length()));
                    } else {
                        expected.add(ent);
                    }
                }
            }
        } else {
            fail("Unexpected file type: "+name);
        }
        try {
            checkArchiveContent(ais, expected);
        } catch (final AssertionFailedError e) {
            fail("Error processing "+file.getName()+" "+e);
        } finally {
            ais.close();
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

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testRandomlySkippingEntries
    public void testRandomlySkippingEntries() throws Exception {
        
        final Map<String, byte[]> entriesByName = new HashMap<String, byte[]>();
        SevenZFile archive = new SevenZFile(getFile("COMPRESS-320/Copy.7z"));
        SevenZArchiveEntry entry;
        while ((entry = archive.getNextEntry()) != null) {
            if (entry.hasStream()) {
                entriesByName.put(entry.getName(), readFully(archive));
            }
        }
        archive.close();

        final String[] variants = {
            "BZip2-solid.7z",
            "BZip2.7z",
            "Copy-solid.7z",
            "Copy.7z",
            "Deflate-solid.7z",
            "Deflate.7z",
            "LZMA-solid.7z",
            "LZMA.7z",
            "LZMA2-solid.7z",
            "LZMA2.7z",
            
            
            
        };

        
        final Random rnd = new Random(0xdeadbeef);
        for (final String fileName : variants) {
            archive = new SevenZFile(getFile("COMPRESS-320/" + fileName));

            while ((entry = archive.getNextEntry()) != null) {
                
                if (rnd.nextBoolean()) {
                    continue;
                }

                if (entry.hasStream()) {
                    assertTrue(entriesByName.containsKey(entry.getName()));
                    final byte [] content = readFully(archive);
                    assertTrue("Content mismatch on: " + fileName + "!" + entry.getName(),
                               Arrays.equals(content, entriesByName.get(entry.getName())));
                }
            }

            archive.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testAllEmptyFilesArchive
    public void testAllEmptyFilesArchive() throws Exception {
        final SevenZFile archive = new SevenZFile(getFile("7z-empty-mhc-off.7z"));
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
        } catch (final PasswordRequiredException ex) {
            final String msg = ex.getMessage();
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
        final SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-256.7z"));
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
        final File output = new File(dir, "lzma2-dictsize.7z");
        final SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            outArchive.setContentMethods(Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.LZMA2, 1 << 20)));
            final SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo.txt");
            outArchive.putArchiveEntry(entry);
            outArchive.write(new byte[] { 'A' });
            outArchive.closeArchiveEntry();
        } finally {
            outArchive.close();
        }

        final SevenZFile archive = new SevenZFile(output);
        try {
            final SevenZArchiveEntry entry = archive.getNextEntry();
            final SevenZMethodConfiguration m = entry.getContentMethods().iterator().next();
            assertEquals(SevenZMethod.LZMA2, m.getMethod());
            assertEquals(1 << 20, m.getOptions());
        } finally {
            archive.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testReadingBackDeltaDistance
    public void testReadingBackDeltaDistance() throws Exception {
        final File output = new File(dir, "delta-distance.7z");
        final SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            outArchive.setContentMethods(Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.DELTA_FILTER, 32),
                                                       new SevenZMethodConfiguration(SevenZMethod.LZMA2)));
            final SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo.txt");
            outArchive.putArchiveEntry(entry);
            outArchive.write(new byte[] { 'A' });
            outArchive.closeArchiveEntry();
        } finally {
            outArchive.close();
        }

        final SevenZFile archive = new SevenZFile(output);
        try {
            final SevenZArchiveEntry entry = archive.getNextEntry();
            final SevenZMethodConfiguration m = entry.getContentMethods().iterator().next();
            assertEquals(SevenZMethod.DELTA_FILTER, m.getMethod());
            assertEquals(32, m.getOptions());
        } finally {
            archive.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::getEntriesOfUnarchiveTest
    public void getEntriesOfUnarchiveTest() throws IOException {
        final SevenZFile sevenZFile = new SevenZFile(getFile("bla.7z"));
        try {
            final Iterable<SevenZArchiveEntry> entries = sevenZFile.getEntries();
            final Iterator<SevenZArchiveEntry> iter = entries.iterator();
            SevenZArchiveEntry entry = iter.next();
            assertEquals("test1.xml", entry.getName());
            entry = iter.next();
            assertEquals("test2.xml", entry.getName());
            assertFalse(iter.hasNext());
        } finally {
            sevenZFile.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::readEntriesOfSize0
    public void readEntriesOfSize0() throws IOException {
        final SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-348.7z"));
        try {
            int entries = 0;
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            while (entry != null) {
                entries++;
                int b = sevenZFile.read();
                if ("2.txt".equals(entry.getName()) || "5.txt".equals(entry.getName())) {
                    assertEquals(-1, b);
                } else {
                    assertNotEquals(-1, b);
                }
                entry = sevenZFile.getNextEntry();
            }
            assertEquals(5, entries);
        } finally {
            sevenZFile.close();
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDirectoriesAndEmptyFiles
    public void testDirectoriesAndEmptyFiles() throws Exception {
        output = new File(dir, "empties.7z");

        final Date accessDate = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        final Date creationDate = cal.getTime();

        final SevenZOutputFile outArchive = new SevenZOutputFile(output);
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
            final byte[] content = new byte[2];
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
        final SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            final SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo/");
            entry.setDirectory(true);
            outArchive.putArchiveEntry(entry);
            outArchive.closeArchiveEntry();
        } finally {
            outArchive.close();
        }

        final SevenZFile archive = new SevenZFile(output);
        try {
            final SevenZArchiveEntry entry = archive.getNextEntry();
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
        final SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            outArchive.finish();
            outArchive.finish();
            fail("shouldn't be able to call finish twice");
        } catch (final IOException ex) {
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
        final ArrayList<SevenZMethodConfiguration> methods = new ArrayList<SevenZMethodConfiguration>();
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
        final LZMA2Options opts = new LZMA2Options(1);
        createAndReadBack(output, Collections
                          .singletonList(new SevenZMethodConfiguration(SevenZMethod.LZMA2, opts)));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testArchiveWithMixedMethods
    public void testArchiveWithMixedMethods() throws Exception {
        output = new File(dir, "mixed-methods.7z");
        final SevenZOutputFile outArchive = new SevenZOutputFile(output);
        try {
            addFile(outArchive, 0, true);
            addFile(outArchive, 1, true, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.BZIP2)));
        } finally {
            outArchive.close();
        }

        final SevenZFile archive = new SevenZFile(output);
        try {
            assertEquals(Boolean.TRUE,
                         verifyFile(archive, 0, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.LZMA2))));
            assertEquals(Boolean.TRUE,
                         verifyFile(archive, 1, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.BZIP2))));
        } finally {
            archive.close();
        }
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddChangeTwice
    public void testAddChangeTwice() throws Exception {
        InputStream in = null;
        InputStream in2 = null;
        try {
            in = new FileInputStream(getFile("test.txt"));
            in2 = new FileInputStream(getFile("test2.xml"));

            final ArchiveEntry e = new ZipArchiveEntry("test.txt");
            final ArchiveEntry e2 = new ZipArchiveEntry("test.txt");

            final ChangeSet changes = new ChangeSet();
            changes.add(e, in);
            changes.add(e2, in2);

            assertEquals(1, changes.getChanges().size());
            final Change c = changes.getChanges().iterator().next();
            assertEquals(in2, c.getInput());
        } finally {
            if (in != null) {
                in.close();
            }
            if (in2 != null) {
                in2.close();
            }
        }
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddChangeTwiceWithoutReplace
    public void testAddChangeTwiceWithoutReplace() throws Exception {
        InputStream in = null;
        InputStream in2 = null;
        try {
            in = new FileInputStream(getFile("test.txt"));
            in2 = new FileInputStream(getFile("test2.xml"));

            final ArchiveEntry e = new ZipArchiveEntry("test.txt");
            final ArchiveEntry e2 = new ZipArchiveEntry("test.txt");

            final ChangeSet changes = new ChangeSet();
            changes.add(e, in, true);
            changes.add(e2, in2, false);

            assertEquals(1, changes.getChanges().size());
            final Change c = changes.getChanges().iterator().next();
            assertEquals(in, c.getInput());
        } finally {
            if (in != null) {
                in.close();
            }
            if (in2 != null) {
                in2.close();
            }
        }
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteDir
    public void testDeleteDir() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();
            changes.deleteDir("bla");
            archiveListDeleteDir("bla");
            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteDir2
    public void testDeleteDir2() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();
            changes.deleteDir("la");
            archiveListDeleteDir("la");
            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteDir3
    public void testDeleteDir3() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();
            changes.deleteDir("test.txt");
            archiveListDeleteDir("test.txt");
            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFile
    public void testDeleteFile() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();
            changes.delete("bla/test5.xml");
            archiveListDelete("bla/test5.xml");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFile2
    public void testDeleteFile2() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();
            changes.delete("bla");
            

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeletePlusAddSame
    public void testDeletePlusAddSame() throws Exception {
        final String archivename = "zip";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();

        File testtxt = null;
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();
            changes.delete("test/test3.xml");
            archiveListDelete("test/test3.xml");

            
            testtxt = getFile("test.txt");
            final ArchiveEntry entry = out.createArchiveEntry(testtxt, "test/test3.xml");
            changes.add(entry, new FileInputStream(testtxt));
            archiveList.add("test/test3.xml");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        
        ArchiveInputStream in = null;
        File check = null;
        try {
            final InputStream is = new FileInputStream(result);
            final BufferedInputStream buf = new BufferedInputStream(is);
            in = factory.createArchiveInputStream(buf);
            check = this.checkArchiveContent(in, archiveList, false);
            final File test3xml = new File(check,"result/test/test3.xml");
            assertEquals(testtxt.length(), test3xml.length());

            final BufferedReader reader = new BufferedReader(new FileReader(test3xml));
            String str;
            while ((str = reader.readLine()) != null) {
                
                "111111111111111111111111111000101011".equals(str);
            }
            reader.close();
        } finally {
            if (in != null) {
                in.close();
            }
            rmdir(check);
        } 
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testChangeSetResults
    public void testChangeSetResults() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();
            changes.deleteDir("bla");
            archiveListDeleteDir("bla");

            
            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = out.createArchiveEntry(file1, "bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            final ChangeSetResults results = performer.perform(ais, out);
            is.close();

            
            assertEquals(1,results.getAddedFromChangeSet().size());
            assertEquals("bla/test.txt",results.getAddedFromChangeSet().iterator().next());
            assertEquals(3,results.getDeleted().size());
            assertTrue(results.getDeleted().contains("bla/test4.xml"));
            assertTrue(results.getDeleted().contains("bla/test5.xml"));
            assertTrue(results.getDeleted().contains("bla/blubber/test6.xml"));

            assertTrue(results.getAddedFromStream().contains("testdata/test1.xml"));
            assertTrue(results.getAddedFromStream().contains("testdata/test2.xml"));
            assertTrue(results.getAddedFromStream().contains("test/test3.xml"));
            assertTrue(results.getAddedFromStream().contains("test.txt"));
            assertTrue(results.getAddedFromStream().contains("something/bla"));
            assertTrue(results.getAddedFromStream().contains("test with spaces.txt"));
            assertEquals(6,results.getAddedFromStream().size());
        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeletePlusAdd
    public void testDeletePlusAdd() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();
            changes.deleteDir("bla");
            archiveListDeleteDir("bla");

            
            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = out.createArchiveEntry(file1, "bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToZip
    public void testDeleteFromAndAddToZip() throws Exception {
        final String archivename = "zip";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = new ZipArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("blub/test.txt");

            changes.delete("testdata/test1.xml");
            archiveListDelete("testdata/test1.xml");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToZipUsingZipFilePerform
    public void testDeleteFromAndAddToZipUsingZipFilePerform() throws Exception {
        final String archivename = "zip";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ZipFile ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            ais = new ZipFile(input);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = new ZipArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("blub/test.txt");

            changes.delete("testdata/test1.xml");
            archiveListDelete("testdata/test1.xml");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddDeleteAdd
    public void testAddDeleteAdd() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = new CpioArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("blub/test.txt");

            changes.deleteDir("blub");
            archiveListDeleteDir("blub");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteAddDelete
    public void testDeleteAddDelete() throws Exception {
        final String archivename = "cpio";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();

            changes.deleteDir("bla");

            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = new CpioArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");

            changes.deleteDir("bla");
            archiveListDeleteDir("bla");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromZip
    public void testDeleteFromZip() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            final ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File input = getFile("bla.zip");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);

            temp = File.createTempFile("test", ".zip");
            temp.deleteOnExit();
            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(temp));

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        final List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");

        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromTar
    public void testDeleteFromTar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            final ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File input = getFile("bla.tar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("tar", is);

            temp = new File(dir, "bla.tar");
            out = factory.createArchiveOutputStream("tar",
                    new FileOutputStream(temp));

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        final List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromJar
    public void testDeleteFromJar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            final ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");
            changes.deleteDir("META-INF");
            changes.delete(".classpath");
            changes.delete(".project");

            final File input = getFile("bla.jar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("jar", is);

            temp = new File(dir, "bla.jar");
            out = factory.createArchiveOutputStream("jar",
                    new FileOutputStream(temp));

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        final List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToTar
    public void testDeleteFromAndAddToTar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            final ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File file1 = getFile("test.txt");

            final TarArchiveEntry entry = new TarArchiveEntry(
                    "testdata/test.txt");
            entry.setModTime(0);
            entry.setSize(file1.length());
            entry.setUserId(0);
            entry.setGroupId(0);
            entry.setUserName("avalon");
            entry.setGroupName("excalibur");
            entry.setMode(0100000);

            changes.add(entry, new FileInputStream(file1));

            final File input = getFile("bla.tar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("tar", is);

            temp = new File(dir, "bla.tar");
            out = factory.createArchiveOutputStream("tar",
                    new FileOutputStream(temp));

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        final List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        expected.add("testdata/test.txt");
        final ArchiveInputStream in = factory.createArchiveInputStream("tar", new FileInputStream(temp));
        this.checkArchiveContent(in, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToJar
    public void testDeleteFromAndAddToJar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            final ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");
            changes.deleteDir("META-INF");
            changes.delete(".classpath");
            changes.delete(".project");

            final File file1 = getFile("test.txt");
            final JarArchiveEntry entry = new JarArchiveEntry("testdata/test.txt");
            changes.add(entry, new FileInputStream(file1));

            final File input = getFile("bla.jar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("jar", is);

            temp = new File(dir, "bla.jar");
            out = factory.createArchiveOutputStream("jar",
                    new FileOutputStream(temp));

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        final List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        expected.add("testdata/test.txt");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAr
    public void testDeleteFromAr() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            final ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File input = getFile("bla.ar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("ar", is);

            temp = new File(dir, "bla.ar");
            out = factory.createArchiveOutputStream("ar",
                    new FileOutputStream(temp));

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        final List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToAr
    public void testDeleteFromAndAddToAr() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            final ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File file1 = getFile("test.txt");

            final ArArchiveEntry entry = new ArArchiveEntry("test.txt", file1
                    .length());

            changes.add(entry, new FileInputStream(file1));

            final File input = getFile("bla.ar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("ar", is);

            temp = new File(dir, "bla.ar");
            out = factory.createArchiveOutputStream("ar",
                    new FileOutputStream(temp));

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        final List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        expected.add("test.txt");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testRenameAndDelete
    public void testRenameAndDelete() throws Exception {
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddMoveDelete
    public void testAddMoveDelete() throws Exception {
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddToEmptyArchive
    public void testAddToEmptyArchive() throws Exception {
        final String archivename = "zip";
        final File input = this.createEmptyArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        InputStream is = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        final ChangeSet changes = new ChangeSet();
        try {

            is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = new ZipArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");
            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close(); 
            } else if (is != null){
                is.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteAddToOneFileArchive
    public void testDeleteAddToOneFileArchive() throws Exception {
        final String archivename = "zip";
        final File input = this.createSingleEntryArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        InputStream is = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        final ChangeSet changes = new ChangeSet();
        try {

            is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));
            changes.delete("test1.xml");
            archiveListDelete("test1.xml");

            final File file = getFile("test.txt");
            final ArchiveEntry entry = out.createArchiveEntry(file,"bla/test.txt");
            changes.add(entry, new FileInputStream(file));
            archiveList.add("bla/test.txt");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close(); 
            } else if (is != null){
                is.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddDeleteToOneFileArchive
    public void testAddDeleteToOneFileArchive() throws Exception {
        final String archivename = "cpio";
        final File input = this.createSingleEntryArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        InputStream is = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        final ChangeSet changes = new ChangeSet();
        try {

            is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));
            final File file = getFile("test.txt");
            final ArchiveEntry entry = out.createArchiveEntry(file,"bla/test.txt"); 
            changes.add(entry, new FileInputStream(file));
            archiveList.add("bla/test.txt");

            changes.delete("test1.xml");
            archiveListDelete("test1.xml");

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close(); 
            } else if (is != null){
                is.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddAllreadyExistingWithReplaceTrue
    public void testAddAllreadyExistingWithReplaceTrue() throws Exception {
        final String archivename = "zip";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = new ZipArchiveEntry("testdata/test1.xml");
            changes.add(entry, new FileInputStream(file1), true);

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            final ChangeSetResults results = performer.perform(ais, out);
            assertTrue(results.getAddedFromChangeSet().contains("testdata/test1.xml"));
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddAllreadyExistingWithReplaceFalse
    public void testAddAllreadyExistingWithReplaceFalse() throws Exception {
        final String archivename = "zip";
        final File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        final File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            final ArchiveEntry entry = new ZipArchiveEntry("testdata/test1.xml");
            changes.add(entry, new FileInputStream(file1), false);

            final ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            final ChangeSetResults results = performer.perform(ais, out);
            assertTrue(results.getAddedFromStream().contains("testdata/test1.xml"));
            assertTrue(results.getAddedFromChangeSet().isEmpty());
            assertTrue(results.getDeleted().isEmpty());
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }
