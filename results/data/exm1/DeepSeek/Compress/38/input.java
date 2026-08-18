// buggy code
    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

        if (getName().endsWith("/")) {
            return true;
        }

        return false;
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

// org.apache.commons.compress.ChainingTestCase::testTarGzip
    public void testTarGzip() throws Exception {
        final File file = getFile("bla.tgz");
        final TarArchiveInputStream is = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
        final TarArchiveEntry entry = (TarArchiveEntry)is.getNextEntry();
        assertNotNull(entry);
        assertEquals("test1.xml", entry.getName());
        is.close();
    }

// org.apache.commons.compress.ChainingTestCase::testTarBzip2
    public void testTarBzip2() throws Exception {
        final File file = getFile("bla.tar.bz2");
        final TarArchiveInputStream is = new TarArchiveInputStream(new BZip2CompressorInputStream(new FileInputStream(file)));
        final TarArchiveEntry entry = (TarArchiveEntry)is.getNextEntry();
        assertNotNull(entry);
        assertEquals("test1.xml", entry.getName());
        is.close();
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

// org.apache.commons.compress.IOMethodsTest::testWriteAr
    public void testWriteAr() throws Exception {
        final ArchiveEntry entry = new ArArchiveEntry("dummy", bytesToTest);
        compareWrites("ar", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testWriteCpio
    public void testWriteCpio() throws Exception {
        final ArchiveEntry entry = new CpioArchiveEntry("dummy", bytesToTest);
        compareWrites("cpio", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testWriteJar
    public void testWriteJar() throws Exception {
        final ArchiveEntry entry = new JarArchiveEntry("dummy");
        compareWrites("jar", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testWriteTar
    public void testWriteTar() throws Exception {
        final TarArchiveEntry entry = new TarArchiveEntry("dummy");
        entry.setSize(bytesToTest);
        compareWrites("tar", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testWriteZip
    public void testWriteZip() throws Exception {
        final ArchiveEntry entry = new ZipArchiveEntry("dummy");
        compareWrites("zip", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testReadAr
    public void testReadAr() throws Exception {
        compareReads("ar");
    }

// org.apache.commons.compress.IOMethodsTest::testReadCpio
    public void testReadCpio() throws Exception {
        compareReads("cpio");
    }

// org.apache.commons.compress.IOMethodsTest::testReadJar
    public void testReadJar() throws Exception {
        compareReads("jar");
    }

// org.apache.commons.compress.IOMethodsTest::testReadTar
    public void testReadTar() throws Exception {
        compareReads("tar");
    }

// org.apache.commons.compress.IOMethodsTest::testReadZip
    public void testReadZip() throws Exception {
        compareReads("zip");
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testFinish
    public void testFinish() throws Exception {
        final OutputStream out1 = new ByteArrayOutputStream();

        ArchiveOutputStream aos1 = factory.createArchiveOutputStream("zip", out1);
        aos1.putArchiveEntry(new ZipArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (final IOException io) {
            
        }

        aos1 = factory.createArchiveOutputStream("jar", out1);
        aos1.putArchiveEntry(new JarArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (final IOException io) {
            
        }

        aos1 = factory.createArchiveOutputStream("ar", out1);
        aos1.putArchiveEntry(new ArArchiveEntry("dummy", 100));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (final IOException io) {
            
        }

        aos1 = factory.createArchiveOutputStream("cpio", out1);
        aos1.putArchiveEntry(new CpioArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (final IOException io) {
            
        }

        aos1 = factory.createArchiveOutputStream("tar", out1);
        aos1.putArchiveEntry(new TarArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (final IOException io) {
            
        }
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testOptionalFinish
    public void testOptionalFinish() throws Exception {
        final OutputStream out1 = new ByteArrayOutputStream();

        ArchiveOutputStream aos1 = factory.createArchiveOutputStream("zip", out1);
        aos1.putArchiveEntry(new ZipArchiveEntry("dummy"));
        aos1.closeArchiveEntry();
        aos1.close();

        aos1 = factory.createArchiveOutputStream("jar", out1);
        aos1.putArchiveEntry(new JarArchiveEntry("dummy"));
        aos1.closeArchiveEntry();
        aos1.close();
        try {
            aos1.finish();
            fail("finish() cannot follow close()");
        } catch (final IOException io) {
            
        }
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testCallSequenceAr
    public void testCallSequenceAr() throws Exception{
        doCallSequence("Ar");
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testCallSequenceCpio
    public void testCallSequenceCpio() throws Exception{
        doCallSequence("Cpio");
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testCallSequenceJar
    public void testCallSequenceJar() throws Exception{
        doCallSequence("Jar");
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testCallSequenceTar
    public void testCallSequenceTar() throws Exception{
        doCallSequence("Tar");
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testCallSequenceZip
    public void testCallSequenceZip() throws Exception{
        doCallSequence("Zip");
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

// org.apache.commons.compress.archivers.TarTestCase::testTarArchiveCreation
    public void testTarArchiveCreation() throws Exception {
        final File output = new File(dir, "bla.tar");
        final File file1 = getFile("test1.xml");
        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("tar", out);
        final TarArchiveEntry entry = new TarArchiveEntry("testdata/test1.xml");
        entry.setModTime(0);
        entry.setSize(file1.length());
        entry.setUserId(0);
        entry.setGroupId(0);
        entry.setUserName("avalon");
        entry.setGroupName("excalibur");
        entry.setMode(0100000);
        os.putArchiveEntry(entry);
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();
        os.close();
    }

// org.apache.commons.compress.archivers.TarTestCase::testTarArchiveLongNameCreation
    public void testTarArchiveLongNameCreation() throws Exception {
        final String name = "testdata/12345678901234567890123456789012345678901234567890123456789012345678901234567890123456.xml";
        final byte[] bytes = name.getBytes(CharsetNames.UTF_8);
        assertEquals(bytes.length, 99);

        final File output = new File(dir, "bla.tar");
        final File file1 = getFile("test1.xml");
        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("tar", out);
        final TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setModTime(0);
        entry.setSize(file1.length());
        entry.setUserId(0);
        entry.setGroupId(0);
        entry.setUserName("avalon");
        entry.setGroupName("excalibur");
        entry.setMode(0100000);
        os.putArchiveEntry(entry);
        final FileInputStream in = new FileInputStream(file1);
        IOUtils.copy(in, os);
        os.closeArchiveEntry();
        os.close();
        out.close();
        in.close();

        ArchiveOutputStream os2 = null;
        try {
            final String toLongName = "testdata/123456789012345678901234567890123456789012345678901234567890123456789012345678901234567.xml";
            final File output2 = new File(dir, "bla.tar");
            final OutputStream out2 = new FileOutputStream(output2);
            os2 = new ArchiveStreamFactory().createArchiveOutputStream("tar", out2);
            final TarArchiveEntry entry2 = new TarArchiveEntry(toLongName);
            entry2.setModTime(0);
            entry2.setSize(file1.length());
            entry2.setUserId(0);
            entry2.setGroupId(0);
            entry2.setUserName("avalon");
            entry2.setGroupName("excalibur");
            entry2.setMode(0100000);
            os2.putArchiveEntry(entry);
            IOUtils.copy(new FileInputStream(file1), os2);
            os2.closeArchiveEntry();
        } catch(final IOException e) {
            assertTrue(true);
        } finally {
            if (os2 != null){
                os2.close();
            }
        }
    }

// org.apache.commons.compress.archivers.TarTestCase::testTarUnarchive
    public void testTarUnarchive() throws Exception {
        final File input = getFile("bla.tar");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("tar", is);
        final TarArchiveEntry entry = (TarArchiveEntry)in.getNextEntry();
        final OutputStream out = new FileOutputStream(new File(dir, entry.getName()));
        IOUtils.copy(in, out);
        in.close();
        out.close();
    }

// org.apache.commons.compress.archivers.TarTestCase::testCOMPRESS114
    public void testCOMPRESS114() throws Exception {
        final File input = getFile("COMPRESS-114.tar");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new TarArchiveInputStream(is,
                CharsetNames.ISO_8859_1);
        TarArchiveEntry entry = (TarArchiveEntry)in.getNextEntry();
        assertEquals("3\u00b1\u00b1\u00b1F06\u00b1W2345\u00b1ZB\u00b1la\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1BLA", entry.getName());
        entry = (TarArchiveEntry)in.getNextEntry();
        assertEquals("0302-0601-3\u00b1\u00b1\u00b1F06\u00b1W2345\u00b1ZB\u00b1la\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1BLA",entry.getName());
        in.close();
    }

// org.apache.commons.compress.archivers.TarTestCase::testDirectoryEntryFromFile
    public void testDirectoryEntryFromFile() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        TarArchiveOutputStream tos = null;
        TarArchiveInputStream tis = null;
        try {
            archive = File.createTempFile("test.", ".tar", tmp[0]);
            archive.deleteOnExit();
            tos = new TarArchiveOutputStream(new FileOutputStream(archive));
            final long beforeArchiveWrite = tmp[0].lastModified();
            final TarArchiveEntry in = new TarArchiveEntry(tmp[0], "foo");
            tos.putArchiveEntry(in);
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new TarArchiveInputStream(new FileInputStream(archive));
            final TarArchiveEntry out = tis.getNextTarEntry();
            tis.close();
            tis = null;
            assertNotNull(out);
            assertEquals("foo/", out.getName());
            assertEquals(0, out.getSize());
            
            assertEquals(beforeArchiveWrite / 1000,
                         out.getLastModifiedDate().getTime() / 1000);
            assertTrue(out.isDirectory());
        } finally {
            if (tis != null) {
                tis.close();
            }
            if (tos != null) {
                tos.close();
            }
            tryHardToDelete(archive);
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.TarTestCase::testExplicitDirectoryEntry
    public void testExplicitDirectoryEntry() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        TarArchiveOutputStream tos = null;
        TarArchiveInputStream tis = null;
        try {
            archive = File.createTempFile("test.", ".tar", tmp[0]);
            archive.deleteOnExit();
            tos = new TarArchiveOutputStream(new FileOutputStream(archive));
            final long beforeArchiveWrite = tmp[0].lastModified();
            final TarArchiveEntry in = new TarArchiveEntry("foo/");
            in.setModTime(beforeArchiveWrite);
            tos.putArchiveEntry(in);
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new TarArchiveInputStream(new FileInputStream(archive));
            final TarArchiveEntry out = tis.getNextTarEntry();
            tis.close();
            tis = null;
            assertNotNull(out);
            assertEquals("foo/", out.getName());
            assertEquals(0, out.getSize());
            assertEquals(beforeArchiveWrite / 1000,
                         out.getLastModifiedDate().getTime() / 1000);
            assertTrue(out.isDirectory());
        } finally {
            if (tis != null) {
                tis.close();
            }
            if (tos != null) {
                tos.close();
            }
            tryHardToDelete(archive);
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.TarTestCase::testFileEntryFromFile
    public void testFileEntryFromFile() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        TarArchiveOutputStream tos = null;
        TarArchiveInputStream tis = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".tar", tmp[0]);
            archive.deleteOnExit();
            tos = new TarArchiveOutputStream(new FileOutputStream(archive));
            final TarArchiveEntry in = new TarArchiveEntry(tmp[1], "foo");
            tos.putArchiveEntry(in);
            final byte[] b = new byte[(int) tmp[1].length()];
            fis = new FileInputStream(tmp[1]);
            while (fis.read(b) > 0) {
                tos.write(b);
            }
            fis.close();
            fis = null;
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new TarArchiveInputStream(new FileInputStream(archive));
            final TarArchiveEntry out = tis.getNextTarEntry();
            tis.close();
            tis = null;
            assertNotNull(out);
            assertEquals("foo", out.getName());
            assertEquals(tmp[1].length(), out.getSize());
            assertEquals(tmp[1].lastModified() / 1000,
                         out.getLastModifiedDate().getTime() / 1000);
            assertFalse(out.isDirectory());
        } finally {
            if (tis != null) {
                tis.close();
            }
            if (tos != null) {
                tos.close();
            }
            tryHardToDelete(archive);
            if (fis != null) {
                fis.close();
            }
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.TarTestCase::testExplicitFileEntry
    public void testExplicitFileEntry() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        TarArchiveOutputStream tos = null;
        TarArchiveInputStream tis = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".tar", tmp[0]);
            archive.deleteOnExit();
            tos = new TarArchiveOutputStream(new FileOutputStream(archive));
            final TarArchiveEntry in = new TarArchiveEntry("foo");
            in.setModTime(tmp[1].lastModified());
            in.setSize(tmp[1].length());
            tos.putArchiveEntry(in);
            final byte[] b = new byte[(int) tmp[1].length()];
            fis = new FileInputStream(tmp[1]);
            while (fis.read(b) > 0) {
                tos.write(b);
            }
            fis.close();
            fis = null;
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new TarArchiveInputStream(new FileInputStream(archive));
            final TarArchiveEntry out = tis.getNextTarEntry();
            tis.close();
            tis = null;
            assertNotNull(out);
            assertEquals("foo", out.getName());
            assertEquals(tmp[1].length(), out.getSize());
            assertEquals(tmp[1].lastModified() / 1000,
                         out.getLastModifiedDate().getTime() / 1000);
            assertFalse(out.isDirectory());
        } finally {
            if (tis != null) {
                tis.close();
            }
            if (tos != null) {
                tos.close();
            }
            tryHardToDelete(archive);
            if (fis != null) {
                fis.close();
            }
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.TarTestCase::testCOMPRESS178
    public void testCOMPRESS178() throws Exception {
        final File input = getFile("COMPRESS-178.tar");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("tar", is);
        try {
            in.getNextEntry();
            fail("Expected IOException");
        } catch (final IOException e) {
            final Throwable t = e.getCause();
            assertTrue("Expected cause = IllegalArgumentException", t instanceof IllegalArgumentException);
        }
        in.close();
    }

// org.apache.commons.compress.archivers.tar.SparseFilesTest::testOldGNU
    public void testOldGNU() throws Throwable {
        final File file = getFile("oldgnu_sparse.tar");
        TarArchiveInputStream tin = null;
        try {
            tin = new TarArchiveInputStream(new FileInputStream(file));
            final TarArchiveEntry ae = tin.getNextTarEntry();
            assertEquals("sparsefile", ae.getName());
            assertTrue(ae.isOldGNUSparse());
            assertTrue(ae.isGNUSparse());
            assertFalse(ae.isPaxGNUSparse());
            assertFalse(tin.canReadEntryData(ae));
        } finally {
            if (tin != null) {
                tin.close();
            }
        }
    }

// org.apache.commons.compress.archivers.tar.SparseFilesTest::testPaxGNU
    public void testPaxGNU() throws Throwable {
        final File file = getFile("pax_gnu_sparse.tar");
        TarArchiveInputStream tin = null;
        try {
            tin = new TarArchiveInputStream(new FileInputStream(file));
            assertPaxGNUEntry(tin, "0.0");
            assertPaxGNUEntry(tin, "0.1");
            assertPaxGNUEntry(tin, "1.0");
        } finally {
            if (tin != null) {
                tin.close();
            }
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testFileSystemRoot
    public void testFileSystemRoot() {
        final TarArchiveEntry t = new TarArchiveEntry(new File(ROOT));
        assertEquals("/", t.getName());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testTarFileWithFSRoot
    public void testTarFileWithFSRoot() throws IOException {
        final File f = File.createTempFile("taetest", ".tar");
        f.deleteOnExit();
        TarArchiveOutputStream tout = null;
        TarArchiveInputStream tin = null;
        try {
            tout = new TarArchiveOutputStream(new FileOutputStream(f));
            TarArchiveEntry t = new TarArchiveEntry(new File(ROOT));
            tout.putArchiveEntry(t);
            tout.closeArchiveEntry();
            t = new TarArchiveEntry(new File(new File(ROOT), "foo.txt"));
            t.setSize(6);
            tout.putArchiveEntry(t);
            tout.write(new byte[] {'h', 'e', 'l', 'l', 'o', ' '});
            tout.closeArchiveEntry();
            t = new TarArchiveEntry(new File(new File(ROOT), "bar.txt")
                                    .getAbsolutePath());
            t.setSize(5);
            tout.putArchiveEntry(t);
            tout.write(new byte[] {'w', 'o', 'r', 'l', 'd'});
            tout.closeArchiveEntry();
            t = new TarArchiveEntry("dummy");
            t.setName(new File(new File(ROOT), "baz.txt").getAbsolutePath());
            t.setSize(1);
            tout.putArchiveEntry(t);
            tout.write(new byte[] {'!'});
            tout.closeArchiveEntry();
            tout.close();
            tout = null;

            tin = new TarArchiveInputStream(new FileInputStream(f));
            
            t = tin.getNextTarEntry();
            assertNotNull(t);
            assertEquals("/", t.getName());
            assertTrue(t.isCheckSumOK());
            t = tin.getNextTarEntry();
            assertNotNull(t);
            assertEquals("foo.txt", t.getName());
            assertTrue(t.isCheckSumOK());
            t = tin.getNextTarEntry();
            assertNotNull(t);
            assertEquals("bar.txt", t.getName());
            assertTrue(t.isCheckSumOK());
            t = tin.getNextTarEntry();
            assertNotNull(t);
            assertEquals("baz.txt", t.getName());
            assertTrue(t.isCheckSumOK());
        } finally {
            if (tin != null) {
                tin.close();
            }
            if (tout != null) {
                tout.close();
            }
            AbstractTestCase.tryHardToDelete(f);
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testMaxFileSize
    public void testMaxFileSize(){
        final TarArchiveEntry t = new TarArchiveEntry("");
        t.setSize(0);
        t.setSize(1);
        try {
            t.setSize(-1);
            fail("Should have generated IllegalArgumentException");
        } catch (final IllegalArgumentException expected) {
        }
        t.setSize(077777777777L);
        t.setSize(0100000000000L);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testLinkFlagConstructor
    public void testLinkFlagConstructor() {
        final TarArchiveEntry t = new TarArchiveEntry("/foo", LF_GNUTYPE_LONGNAME);
        assertGnuMagic(t);
        assertEquals("foo", t.getName());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testLinkFlagConstructorWithFileFlag
    public void testLinkFlagConstructorWithFileFlag() {
        final TarArchiveEntry t = new TarArchiveEntry("/foo", LF_NORMAL);
        assertPosixMagic(t);
        assertEquals("foo", t.getName());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testLinkFlagConstructorWithPreserve
    public void testLinkFlagConstructorWithPreserve() {
        final TarArchiveEntry t = new TarArchiveEntry("/foo", LF_GNUTYPE_LONGNAME,
                                                true);
        assertGnuMagic(t);
        assertEquals("/foo", t.getName());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readSimplePaxHeader
    public void readSimplePaxHeader() throws Exception {
        final InputStream is = new ByteArrayInputStream(new byte[1]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(is);
        final Map<String, String> headers = tais
            .parsePaxHeaders(new ByteArrayInputStream("30 atime=1321711775.972059463\n"
                                                      .getBytes(CharsetNames.UTF_8)));
        assertEquals(1, headers.size());
        assertEquals("1321711775.972059463", headers.get("atime"));
        tais.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::secondEntryWinsWhenPaxHeaderContainsDuplicateKey
    public void secondEntryWinsWhenPaxHeaderContainsDuplicateKey() throws Exception {
        final InputStream is = new ByteArrayInputStream(new byte[1]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(is);
        final Map<String, String> headers = tais
            .parsePaxHeaders(new ByteArrayInputStream("11 foo=bar\n11 foo=baz\n"
                                                      .getBytes(CharsetNames.UTF_8)));
        assertEquals(1, headers.size());
        assertEquals("baz", headers.get("foo"));
        tais.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::paxHeaderEntryWithEmptyValueRemovesKey
    public void paxHeaderEntryWithEmptyValueRemovesKey() throws Exception {
        final InputStream is = new ByteArrayInputStream(new byte[1]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(is);
        final Map<String, String> headers = tais
            .parsePaxHeaders(new ByteArrayInputStream("11 foo=bar\n7 foo=\n"
                                                      .getBytes(CharsetNames.UTF_8)));
        assertEquals(0, headers.size());
        tais.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readPaxHeaderWithEmbeddedNewline
    public void readPaxHeaderWithEmbeddedNewline() throws Exception {
        final InputStream is = new ByteArrayInputStream(new byte[1]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(is);
        final Map<String, String> headers = tais
            .parsePaxHeaders(new ByteArrayInputStream("28 comment=line1\nline2\nand3\n"
                                                      .getBytes(CharsetNames.UTF_8)));
        assertEquals(1, headers.size());
        assertEquals("line1\nline2\nand3", headers.get("comment"));
        tais.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readNonAsciiPaxHeader
    public void readNonAsciiPaxHeader() throws Exception {
        final String ae = "\u00e4";
        final String line = "11 path="+ ae + "\n";
        assertEquals(11, line.getBytes(CharsetNames.UTF_8).length);
        final InputStream is = new ByteArrayInputStream(new byte[1]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(is);
        final Map<String, String> headers = tais
            .parsePaxHeaders(new ByteArrayInputStream(line.getBytes(CharsetNames.UTF_8)));
        assertEquals(1, headers.size());
        assertEquals(ae, headers.get("path"));
        tais.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::workaroundForBrokenTimeHeader
    public void workaroundForBrokenTimeHeader() throws Exception {
        TarArchiveInputStream in = null;
        try {
            in = new TarArchiveInputStream(new FileInputStream(getFile("simple-aix-native-tar.tar")));
            TarArchiveEntry tae = in.getNextTarEntry();
            tae = in.getNextTarEntry();
            assertEquals("sample/link-to-txt-file.lnk", tae.getName());
            assertEquals(new Date(0), tae.getLastModifiedDate());
            assertTrue(tae.isSymbolicLink());
            assertTrue(tae.isCheckSumOK());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::datePriorToEpochInGNUFormat
    public void datePriorToEpochInGNUFormat() throws Exception {
        datePriorToEpoch("preepoch-star.tar");
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::datePriorToEpochInPAXFormat
    public void datePriorToEpochInPAXFormat() throws Exception {
        datePriorToEpoch("preepoch-posix.tar");
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::testCompress197
    public void testCompress197() throws Exception {
        final TarArchiveInputStream tar = getTestStream("/COMPRESS-197.tar");
        try {
            TarArchiveEntry entry = tar.getNextTarEntry();
            while (entry != null) {
                entry = tar.getNextTarEntry();
            }
        } catch (final IOException e) {
            fail("COMPRESS-197: " + e.getMessage());
        } finally {
            tar.close();
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::shouldUseSpecifiedEncodingWhenReadingGNULongNames
    public void shouldUseSpecifiedEncodingWhenReadingGNULongNames()
        throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final String encoding = CharsetNames.UTF_16;
        final String name = "1234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890\u00e4";
        final TarArchiveOutputStream tos =
            new TarArchiveOutputStream(bos, encoding);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        TarArchiveEntry t = new TarArchiveEntry(name);
        t.setSize(1);
        tos.putArchiveEntry(t);
        tos.write(30);
        tos.closeArchiveEntry();
        tos.close();
        final byte[] data = bos.toByteArray();
        final ByteArrayInputStream bis = new ByteArrayInputStream(data);
        final TarArchiveInputStream tis =
            new TarArchiveInputStream(bis, encoding);
        t = tis.getNextTarEntry();
        assertEquals(name, t.getName());
        tis.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::shouldConsumeArchiveCompletely
    public void shouldConsumeArchiveCompletely() throws Exception {
        final InputStream is = TarArchiveInputStreamTest.class
            .getResourceAsStream("/archive_with_trailer.tar");
        final TarArchiveInputStream tar = new TarArchiveInputStream(is);
        while (tar.getNextTarEntry() != null) {
            
        }
        final byte[] expected = new byte[] {
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', '\n'
        };
        final byte[] actual = new byte[expected.length];
        is.read(actual);
        assertArrayEquals(expected, actual);
        tar.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readsArchiveCompletely_COMPRESS245
    public void readsArchiveCompletely_COMPRESS245() throws Exception {
        final InputStream is = TarArchiveInputStreamTest.class
            .getResourceAsStream("/COMPRESS-245.tar.gz");
        try {
            final InputStream gin = new GZIPInputStream(is);
            final TarArchiveInputStream tar = new TarArchiveInputStream(gin);
            int count = 0;
            TarArchiveEntry entry = tar.getNextTarEntry();
            while (entry != null) {
                count++;
                entry = tar.getNextTarEntry();
            }
            assertEquals(31, count);
            tar.close();
        } catch (final IOException e) {
            fail("COMPRESS-245: " + e.getMessage());
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::shouldThrowAnExceptionOnTruncatedEntries
    public void shouldThrowAnExceptionOnTruncatedEntries() throws Exception {
        final File dir = mkdir("COMPRESS-279");
        final TarArchiveInputStream is = getTestStream("/COMPRESS-279.tar");
        FileOutputStream out = null;
        try {
            TarArchiveEntry entry = is.getNextTarEntry();
            int count = 0;
            while (entry != null) {
                out = new FileOutputStream(new File(dir, String.valueOf(count)));
                IOUtils.copy(is, out);
                out.close();
                out = null;
                count++;
                entry = is.getNextTarEntry();
            }
        } finally {
            is.close();
            if (out != null) {
                out.close();
            }
            rmdir(dir);
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::shouldReadBigGid
    public void shouldReadBigGid() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry t = new TarArchiveEntry("name");
        t.setGroupId(4294967294l);
        t.setSize(1);
        tos.putArchiveEntry(t);
        tos.write(30);
        tos.closeArchiveEntry();
        tos.close();
        final byte[] data = bos.toByteArray();
        final ByteArrayInputStream bis = new ByteArrayInputStream(data);
        final TarArchiveInputStream tis =
            new TarArchiveInputStream(bis);
        t = tis.getNextTarEntry();
        assertEquals(4294967294l, t.getLongGroupId());
        tis.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::shouldReadGNULongNameEntryWithWrongName
    public void shouldReadGNULongNameEntryWithWrongName() throws Exception {
        final TarArchiveInputStream is = getTestStream("/COMPRESS-324.tar");
        try {
            final TarArchiveEntry entry = is.getNextTarEntry();
            assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890"
                         + "1234567890123456789012345678901234567890123456789012345678901234567890"
                         + "1234567890123456789012345678901234567890123456789012345678901234567890"
                         + "1234567890123456789012345678901234567890.txt",
                         entry.getName());
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::survivesBlankLinesInPaxHeader
    public void survivesBlankLinesInPaxHeader() throws Exception {
        final TarArchiveInputStream is = getTestStream("/COMPRESS-355.tar");
        try {
            final TarArchiveEntry entry = is.getNextTarEntry();
            assertEquals("package/package.json", entry.getName());
            assertNull(is.getNextTarEntry());
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::survivesPaxHeaderWithNameEndingInSlash
    public void survivesPaxHeaderWithNameEndingInSlash() throws Exception {
        final TarArchiveInputStream is = getTestStream("/COMPRESS-356.tar");
        try {
            final TarArchiveEntry entry = is.getNextTarEntry();
            assertEquals("package/package.json", entry.getName());
            assertNull(is.getNextTarEntry());
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testCount
    public void testCount() throws Exception {
        final File f = File.createTempFile("commons-compress-tarcount", ".tar");
        f.deleteOnExit();
        final FileOutputStream fos = new FileOutputStream(f);

        final ArchiveOutputStream tarOut = new ArchiveStreamFactory()
            .createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);

        final File file1 = getFile("test1.xml");
        final TarArchiveEntry sEntry = new TarArchiveEntry(file1, file1.getName());
        tarOut.putArchiveEntry(sEntry);

        final FileInputStream in = new FileInputStream(file1);
        final byte[] buf = new byte[8192];

        int read = 0;
        while ((read = in.read(buf)) > 0) {
            tarOut.write(buf, 0, read);
        }

        in.close();
        tarOut.closeArchiveEntry();
        tarOut.close();

        assertEquals(f.length(), tarOut.getBytesWritten());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testMaxFileSizeError
    public void testMaxFileSizeError() throws Exception {
        final TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(077777777777L);
        TarArchiveOutputStream tos =
            new TarArchiveOutputStream(new ByteArrayOutputStream());
        tos.putArchiveEntry(t);
        t.setSize(0100000000000L);
        tos = new TarArchiveOutputStream(new ByteArrayOutputStream());
        try {
            tos.putArchiveEntry(t);
            fail("Should have generated RuntimeException");
        } catch (final RuntimeException expected) {
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testBigNumberStarMode
    public void testBigNumberStarMode() throws Exception {
        final TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(0100000000000L);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        final byte[] data = bos.toByteArray();
        assertEquals(0x80,
                     data[TarConstants.NAMELEN
                        + TarConstants.MODELEN
                        + TarConstants.UIDLEN
                        + TarConstants.GIDLEN] & 0x80);
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(0100000000000L, e.getSize());
        tin.close();
        
        
        closeQuietly(tos);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testBigNumberPosixMode
    public void testBigNumberPosixMode() throws Exception {
        final TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(0100000000000L);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        final byte[] data = bos.toByteArray();
        assertEquals("00000000000 ",
                     new String(data,
                                1024 + TarConstants.NAMELEN
                                + TarConstants.MODELEN
                                + TarConstants.UIDLEN
                                + TarConstants.GIDLEN, 12,
                                CharsetNames.UTF_8));
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(0100000000000L, e.getSize());
        tin.close();
        
        
        closeQuietly(tos);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteSimplePaxHeaders
    public void testWriteSimplePaxHeaders() throws Exception {
        final Map<String, String> m = new HashMap<String, String>();
        m.put("a", "b");
        final byte[] data = writePaxHeader(m);
        assertEquals("00000000006 ",
                     new String(data, TarConstants.NAMELEN
                                + TarConstants.MODELEN
                                + TarConstants.UIDLEN
                                + TarConstants.GIDLEN, 12,
                                CharsetNames.UTF_8));
        assertEquals("6 a=b\n", new String(data, 512, 6, CharsetNames.UTF_8));
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testPaxHeadersWithLength99
    public void testPaxHeadersWithLength99() throws Exception {
        final Map<String, String> m = new HashMap<String, String>();
        m.put("a",
              "0123456789012345678901234567890123456789"
              + "01234567890123456789012345678901234567890123456789"
              + "012");
        final byte[] data = writePaxHeader(m);
        assertEquals("00000000143 ",
                     new String(data, TarConstants.NAMELEN
                                + TarConstants.MODELEN
                                + TarConstants.UIDLEN
                                + TarConstants.GIDLEN, 12,
                                CharsetNames.UTF_8));
        assertEquals("99 a=0123456789012345678901234567890123456789"
              + "01234567890123456789012345678901234567890123456789"
              + "012\n", new String(data, 512, 99, CharsetNames.UTF_8));
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testPaxHeadersWithLength101
    public void testPaxHeadersWithLength101() throws Exception {
        final Map<String, String> m = new HashMap<String, String>();
        m.put("a",
              "0123456789012345678901234567890123456789"
              + "01234567890123456789012345678901234567890123456789"
              + "0123");
        final byte[] data = writePaxHeader(m);
        assertEquals("00000000145 ",
                     new String(data, TarConstants.NAMELEN
                                + TarConstants.MODELEN
                                + TarConstants.UIDLEN
                                + TarConstants.GIDLEN, 12,
                                CharsetNames.UTF_8));
        assertEquals("101 a=0123456789012345678901234567890123456789"
              + "01234567890123456789012345678901234567890123456789"
              + "0123\n", new String(data, 512, 101, CharsetNames.UTF_8));
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongFileNamePosixMode
    public void testWriteLongFileNamePosixMode() throws Exception {
        final String n = "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789";
        final TarArchiveEntry t =
            new TarArchiveEntry(n);
        t.setSize(10 * 1024);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tos.putArchiveEntry(t);
        tos.write(new byte[10 * 1024]);
        tos.closeArchiveEntry();
        final byte[] data = bos.toByteArray();
        assertEquals("160 path=" + n + "\n",
                     new String(data, 512, 160, CharsetNames.UTF_8));
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        tin.close();
        tos.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testOldEntryStarMode
    public void testOldEntryStarMode() throws Exception {
        final TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(Integer.MAX_VALUE);
        t.setModTime(-1000);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        final byte[] data = bos.toByteArray();
        assertEquals((byte) 0xff,
                     data[TarConstants.NAMELEN
                          + TarConstants.MODELEN
                          + TarConstants.UIDLEN
                          + TarConstants.GIDLEN
                          + TarConstants.SIZELEN]);
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1969, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), e.getLastModifiedDate());
        tin.close();
        
        
        closeQuietly(tos);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testOldEntryPosixMode
    public void testOldEntryPosixMode() throws Exception {
        final TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(Integer.MAX_VALUE);
        t.setModTime(-1000);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        final byte[] data = bos.toByteArray();
        assertEquals("00000000000 ",
                     new String(data,
                                1024 + TarConstants.NAMELEN
                                + TarConstants.MODELEN
                                + TarConstants.UIDLEN
                                + TarConstants.GIDLEN
                                + TarConstants.SIZELEN, 12,
                                CharsetNames.UTF_8));
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1969, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), e.getLastModifiedDate());
        tin.close();
        
        
        closeQuietly(tos);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testOldEntryError
    public void testOldEntryError() throws Exception {
        final TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(Integer.MAX_VALUE);
        t.setModTime(-1000);
        final TarArchiveOutputStream tos =
            new TarArchiveOutputStream(new ByteArrayOutputStream());
        try {
            tos.putArchiveEntry(t);
            fail("Should have generated RuntimeException");
        } catch (final RuntimeException expected) {
        }
        tos.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteNonAsciiPathNamePaxHeader
    public void testWriteNonAsciiPathNamePaxHeader() throws Exception {
        final String n = "\u00e4";
        final TarArchiveEntry t = new TarArchiveEntry(n);
        t.setSize(10 * 1024);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.write(new byte[10 * 1024]);
        tos.closeArchiveEntry();
        tos.close();
        final byte[] data = bos.toByteArray();
        assertEquals("11 path=" + n + "\n",
                     new String(data, 512, 11, CharsetNames.UTF_8));
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteNonAsciiLinkPathNamePaxHeader
    public void testWriteNonAsciiLinkPathNamePaxHeader() throws Exception {
        final String n = "\u00e4";
        final TarArchiveEntry t = new TarArchiveEntry("a", TarConstants.LF_LINK);
        t.setSize(10 * 1024);
        t.setLinkName(n);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.write(new byte[10 * 1024]);
        tos.closeArchiveEntry();
        tos.close();
        final byte[] data = bos.toByteArray();
        assertEquals("15 linkpath=" + n + "\n",
                     new String(data, 512, 15, CharsetNames.UTF_8));
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getLinkName());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testRoundtripWith67CharFileNameGnu
    public void testRoundtripWith67CharFileNameGnu() throws Exception {
        testRoundtripWith67CharFileName(TarArchiveOutputStream.LONGFILE_GNU);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testRoundtripWith67CharFileNamePosix
    public void testRoundtripWith67CharFileNamePosix() throws Exception {
        testRoundtripWith67CharFileName(TarArchiveOutputStream.LONGFILE_POSIX);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongDirectoryNameErrorMode
    public void testWriteLongDirectoryNameErrorMode() throws Exception {
        final String n = "01234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789/";

        try {
            final TarArchiveEntry t = new TarArchiveEntry(n);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
            tos.putArchiveEntry(t);
            tos.closeArchiveEntry();
            tos.close();

            fail("Truncated name didn't throw an exception");
        } catch (final RuntimeException e) {
            
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongDirectoryNameTruncateMode
    public void testWriteLongDirectoryNameTruncateMode() throws Exception {
        final String n = "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789/";
        final TarArchiveEntry t = new TarArchiveEntry(n);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        tos.putArchiveEntry(t);
        tos.closeArchiveEntry();
        tos.close();
        final byte[] data = bos.toByteArray();
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals("Entry name", n.substring(0, TarConstants.NAMELEN) + "/", e.getName());
        assertTrue("The entry is not a directory", e.isDirectory());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongDirectoryNameGnuMode
    public void testWriteLongDirectoryNameGnuMode() throws Exception {
        testWriteLongDirectoryName(TarArchiveOutputStream.LONGFILE_GNU);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongDirectoryNamePosixMode
    public void testWriteLongDirectoryNamePosixMode() throws Exception {
        testWriteLongDirectoryName(TarArchiveOutputStream.LONGFILE_POSIX);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteNonAsciiDirectoryNamePosixMode
    public void testWriteNonAsciiDirectoryNamePosixMode() throws Exception {
        final String n = "f\u00f6\u00f6/";
        final TarArchiveEntry t = new TarArchiveEntry(n);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.closeArchiveEntry();
        tos.close();
        final byte[] data = bos.toByteArray();
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        assertTrue(e.isDirectory());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteNonAsciiNameWithUnfortunateNamePosixMode
    public void testWriteNonAsciiNameWithUnfortunateNamePosixMode() throws Exception {
        final String n = "f\u00f6\u00f6\u00dc";
        final TarArchiveEntry t = new TarArchiveEntry(n);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.closeArchiveEntry();
        tos.close();
        final byte[] data = bos.toByteArray();
        final TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        assertFalse(e.isDirectory());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongLinkNameErrorMode
    public void testWriteLongLinkNameErrorMode() throws Exception {
        final String linkname = "01234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789/test";
        final TarArchiveEntry entry = new TarArchiveEntry("test", TarConstants.LF_SYMLINK);
        entry.setLinkName(linkname);

        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
            tos.putArchiveEntry(entry);
            tos.closeArchiveEntry();
            tos.close();

            fail("Truncated link name didn't throw an exception");
        } catch (final RuntimeException e) {
            
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongLinkNameTruncateMode
    public void testWriteLongLinkNameTruncateMode() throws Exception {
        final String linkname = "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789/";
        final TarArchiveEntry entry = new TarArchiveEntry("test" , TarConstants.LF_SYMLINK);
        entry.setLinkName(linkname);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        final byte[] data = bos.toByteArray();
        final TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(data));
        final TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals("Link name", linkname.substring(0, TarConstants.NAMELEN), e.getLinkName());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongLinkNameGnuMode
    public void testWriteLongLinkNameGnuMode() throws Exception {
        testWriteLongLinkName(TarArchiveOutputStream.LONGFILE_GNU);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongLinkNamePosixMode
    public void testWriteLongLinkNamePosixMode() throws Exception {
        testWriteLongLinkName(TarArchiveOutputStream.LONGFILE_POSIX);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testPadsOutputToFullBlockLength
    public void testPadsOutputToFullBlockLength() throws Exception {
        final File f = File.createTempFile("commons-compress-padding", ".tar");
        f.deleteOnExit();
        final FileOutputStream fos = new FileOutputStream(f);
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(fos);
        final File file1 = getFile("test1.xml");
        final TarArchiveEntry sEntry = new TarArchiveEntry(file1, file1.getName());
        tos.putArchiveEntry(sEntry);
        final FileInputStream in = new FileInputStream(file1);
        IOUtils.copy(in, tos);
        in.close();
        tos.closeArchiveEntry();
        tos.close();
        
        assertEquals(TarConstants.DEFAULT_BLKSIZE, f.length());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testLongNameMd5Hash
    public void testLongNameMd5Hash() throws Exception {
        final String longFileName = "a/considerably/longer/file/name/which/forces/use/of/the/long/link/header/which/appears/to/always/use/the/current/time/as/modification/date";
        final String fname = longFileName;
        final Date modificationDate = new Date();

        final byte[] archive1 = createTarArchiveContainingOneDirectory(fname, modificationDate);
        final byte[] digest1 = MessageDigest.getInstance("MD5").digest(archive1);

        
        Thread.sleep(1000L);

        
        final byte[] archive2 = createTarArchiveContainingOneDirectory(fname, modificationDate);
        
        final byte[] digest2 = MessageDigest.getInstance("MD5").digest(archive2);

        Assert.assertArrayEquals(digest1, digest2);

        
        
        Thread.sleep(1000);
        final TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(archive2));
        final ArchiveEntry nextEntry = tarIn.getNextEntry();
        assertEquals(longFileName, nextEntry.getName());
        
        assertEquals(modificationDate.getTime() / 1000, nextEntry.getLastModifiedDate().getTime() / 1000);
        tarIn.close();
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
