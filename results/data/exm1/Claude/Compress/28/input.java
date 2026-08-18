// buggy code
    public int read(byte[] buf, int offset, int numToRead) throws IOException {
    	int totalRead = 0;

        if (hasHitEOF || entryOffset >= entrySize) {
            return -1;
        }

        if (currEntry == null) {
            throw new IllegalStateException("No current tar entry");
        }

        numToRead = Math.min(numToRead, available());
        
        totalRead = is.read(buf, offset, numToRead);
        count(totalRead);
        
        if (totalRead == -1) {
            hasHitEOF = true;
        } else {
            entryOffset += totalRead;
        }

        return totalRead;
    }

// relevant test
// org.apache.commons.compress.ArchiveUtilsTest::testCompareBA
    public void testCompareBA(){
        byte[] buffer1 = {1,2,3};
        byte[] buffer2 = {1,2,3,0};
        byte[] buffer3 = {1,2,3};
        assertTrue(ArchiveUtils.isEqual(buffer1, buffer2, true));
        assertFalse(ArchiveUtils.isEqual(buffer1, buffer2, false));
        assertFalse(ArchiveUtils.isEqual(buffer1, buffer2));
        assertTrue(ArchiveUtils.isEqual(buffer2, buffer1, true));
        assertFalse(ArchiveUtils.isEqual(buffer2, buffer1, false));
        assertFalse(ArchiveUtils.isEqual(buffer2, buffer1));
        assertTrue(ArchiveUtils.isEqual(buffer1, buffer3));
        assertTrue(ArchiveUtils.isEqual(buffer3, buffer1));
    }

// org.apache.commons.compress.ArchiveUtilsTest::testCompareAscii
    public void testCompareAscii(){
        byte[] buffer1 = {'a','b','c'};
        byte[] buffer2 = {'d','e','f',0};
        assertTrue(ArchiveUtils.matchAsciiBuffer("abc", buffer1));
        assertFalse(ArchiveUtils.matchAsciiBuffer("abc\0", buffer1));
        assertTrue(ArchiveUtils.matchAsciiBuffer("def\0", buffer2));
        assertFalse(ArchiveUtils.matchAsciiBuffer("def", buffer2));
    }

// org.apache.commons.compress.ArchiveUtilsTest::testAsciiConversions
    public void testAsciiConversions() {
        asciiToByteAndBackOK("");
        asciiToByteAndBackOK("abcd");
        asciiToByteAndBackFail("\u8025");
    }

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

// org.apache.commons.compress.DetectArchiverTestCase::testDetectionNotArchive
    public void testDetectionNotArchive() throws IOException {
        try {
            getStreamFor("test.txt");
            fail("Expected ArchiveException");
        } catch (ArchiveException e) {
            
        }
    }

// org.apache.commons.compress.DetectArchiverTestCase::testCOMPRESS117
    public void testCOMPRESS117() throws Exception {
        final ArchiveInputStream tar = getStreamFor("COMPRESS-117.tar");
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
        ArchiveEntry entry = new ArArchiveEntry("dummy", bytesToTest);
        compareWrites("ar", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testWriteCpio
    public void testWriteCpio() throws Exception {
        ArchiveEntry entry = new CpioArchiveEntry("dummy", bytesToTest);
        compareWrites("cpio", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testWriteJar
    public void testWriteJar() throws Exception {
        ArchiveEntry entry = new JarArchiveEntry("dummy");
        compareWrites("jar", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testWriteTar
    public void testWriteTar() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("dummy");
        entry.setSize(bytesToTest);
        compareWrites("tar", entry);
    }

// org.apache.commons.compress.IOMethodsTest::testWriteZip
    public void testWriteZip() throws Exception {
        ArchiveEntry entry = new ZipArchiveEntry("dummy");
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

// org.apache.commons.compress.archivers.ArTestCase::testArArchiveCreation
    public void testArArchiveCreation() throws Exception {
        final File output = new File(dir, "bla.ar");

        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");

        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
        os.putArchiveEntry(new ArArchiveEntry("test1.xml", file1.length()));
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();

        os.putArchiveEntry(new ArArchiveEntry("test2.xml", file2.length()));
        IOUtils.copy(new FileInputStream(file2), os);
        os.closeArchiveEntry();

        os.close();
    }

// org.apache.commons.compress.archivers.ArTestCase::testArUnarchive
    public void testArUnarchive() throws Exception {
        final File output = new File(dir, "bla.ar");
        {
            final File file1 = getFile("test1.xml");
            final File file2 = getFile("test2.xml");

            final OutputStream out = new FileOutputStream(output);
            final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
            os.putArchiveEntry(new ArArchiveEntry("test1.xml", file1.length()));
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ArArchiveEntry("test2.xml", file2.length()));
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();
            os.close();
            out.close();
        }

        
        final File input = output;
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
        final ArArchiveEntry entry = (ArArchiveEntry)in.getNextEntry();

        File target = new File(dir, entry.getName());
        final OutputStream out = new FileOutputStream(target);

        IOUtils.copy(in, out);

        out.close();
        in.close();
        is.close();
    }

// org.apache.commons.compress.archivers.ArTestCase::testArDelete
    public void testArDelete() throws Exception {
        final File output = new File(dir, "bla.ar");

        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");
        {
            

            final OutputStream out = new FileOutputStream(output);
            final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
            os.putArchiveEntry(new ArArchiveEntry("test1.xml", file1.length()));
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ArArchiveEntry("test2.xml", file2.length()));
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();
            os.close();
            out.close();
        }

        assertEquals(8
                     + 60 + file1.length() + (file1.length() % 2)
                     + 60 + file2.length() + (file2.length() % 2),
                     output.length());

        final File output2 = new File(dir, "bla2.ar");

        int copied = 0;
        int deleted = 0;

        {
            

            final InputStream is = new FileInputStream(output);
            final OutputStream os = new FileOutputStream(output2);
            final ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream("ar", os);
            final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
            while(true) {
                final ArArchiveEntry entry = (ArArchiveEntry)ais.getNextEntry();
                if (entry == null) {
                    break;
                }

                if ("test1.xml".equals(entry.getName())) {
                    aos.putArchiveEntry(entry);
                    IOUtils.copy(ais, aos);
                    aos.closeArchiveEntry();
                    copied++;
                } else {
                    IOUtils.copy(ais, new ByteArrayOutputStream());
                    deleted++;
                }

            }
            ais.close();
            aos.close();
            is.close();
            os.close();
        }

        assertEquals(1, copied);
        assertEquals(1, deleted);
        assertEquals(8
                     + 60 + file1.length() + (file1.length() % 2),
                     output2.length());

        long files = 0;
        long sum = 0;

        {
            final InputStream is = new FileInputStream(output2);
            final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
            while(true) {
                final ArArchiveEntry entry = (ArArchiveEntry)ais.getNextEntry();
                if (entry == null) {
                    break;
                }

                IOUtils.copy(ais, new ByteArrayOutputStream());

                sum +=  entry.getLength();
                files++;
            }
            ais.close();
            is.close();
        }

        assertEquals(1, files);
        assertEquals(file1.length(), sum);

    }

// org.apache.commons.compress.archivers.ArTestCase::testFileEntryFromFile
    public void testFileEntryFromFile() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        ArArchiveOutputStream aos = null;
        ArArchiveInputStream ais = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".ar", tmp[0]);
            archive.deleteOnExit();
            aos = new ArArchiveOutputStream(new FileOutputStream(archive));
            ArArchiveEntry in = new ArArchiveEntry(tmp[1], "foo");
            aos.putArchiveEntry(in);
            byte[] b = new byte[(int) tmp[1].length()];
            fis = new FileInputStream(tmp[1]);
            while (fis.read(b) > 0) {
                aos.write(b);
            }
            fis.close();
            fis = null;
            aos.closeArchiveEntry();
            aos.close();
            aos = null;
            ais = new ArArchiveInputStream(new FileInputStream(archive));
            ArArchiveEntry out = ais.getNextArEntry();
            ais.close();
            ais = null;
            assertNotNull(out);
            assertEquals("foo", out.getName());
            assertEquals(tmp[1].length(), out.getSize());
            
            assertEquals(tmp[1].lastModified() / 1000,
                         out.getLastModifiedDate().getTime() / 1000);
            assertFalse(out.isDirectory());
        } finally {
            if (ais != null) {
                ais.close();
            }
            if (aos != null) {
                aos.close();
            }
            tryHardToDelete(archive);
            if (fis != null) {
                fis.close();
            }
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.ArTestCase::testExplicitFileEntry
    public void testExplicitFileEntry() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        ArArchiveOutputStream aos = null;
        ArArchiveInputStream ais = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".ar", tmp[0]);
            archive.deleteOnExit();
            aos = new ArArchiveOutputStream(new FileOutputStream(archive));
            ArArchiveEntry in = new ArArchiveEntry("foo", tmp[1].length(),
                                                   0, 0, 0,
                                                   tmp[1].lastModified() / 1000);
            aos.putArchiveEntry(in);
            byte[] b = new byte[(int) tmp[1].length()];
            fis = new FileInputStream(tmp[1]);
            while (fis.read(b) > 0) {
                aos.write(b);
            }
            fis.close();
            fis = null;
            aos.closeArchiveEntry();
            aos.close();
            aos = null;
            ais = new ArArchiveInputStream(new FileInputStream(archive));
            ArArchiveEntry out = ais.getNextArEntry();
            ais.close();
            ais = null;
            assertNotNull(out);
            assertEquals("foo", out.getName());
            assertEquals(tmp[1].length(), out.getSize());
            assertEquals(tmp[1].lastModified() / 1000,
                         out.getLastModifiedDate().getTime() / 1000);
            assertFalse(out.isDirectory());
        } finally {
            if (ais != null) {
                ais.close();
            }
            if (aos != null) {
                aos.close();
            }
            tryHardToDelete(archive);
            if (fis != null) {
                fis.close();
            }
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testFinish
    public void testFinish() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();

        ArchiveOutputStream aos1 = factory.createArchiveOutputStream("zip", out1);
        aos1.putArchiveEntry(new ZipArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            
        }

        aos1 = factory.createArchiveOutputStream("jar", out1);
        aos1.putArchiveEntry(new JarArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            
        }

        aos1 = factory.createArchiveOutputStream("ar", out1);
        aos1.putArchiveEntry(new ArArchiveEntry("dummy", 100));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            
        }

        aos1 = factory.createArchiveOutputStream("cpio", out1);
        aos1.putArchiveEntry(new CpioArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            
        }

        aos1 = factory.createArchiveOutputStream("tar", out1);
        aos1.putArchiveEntry(new TarArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            
        }
    }

// org.apache.commons.compress.archivers.ArchiveOutputStreamTest::testOptionalFinish
    public void testOptionalFinish() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();

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
        } catch (IOException io) {
            
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
        } catch (ArchiveException ae) {
            assertTrue(ae.getMessage().startsWith("No Archiver found"));
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::aiffFilesAreNoTARs
    public void aiffFilesAreNoTARs() throws Exception {
    	FileInputStream fis = new FileInputStream("src/test/resources/testAIFF.aif");
    	try {
            InputStream is = new BufferedInputStream(fis);
            try {
                new ArchiveStreamFactory().createArchiveInputStream(is);
                fail("created an input stream for a non-archive");
            } catch (ArchiveException ae) {
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
    	FileInputStream fis = new FileInputStream("src/test/resources/testCompress209.doc");
    	try {
            InputStream bis = new BufferedInputStream(fis);
            try {
                new ArchiveStreamFactory().createArchiveInputStream(bis);
                fail("created an input stream for a non-archive");
            } catch (ArchiveException ae) {
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
    	FileInputStream fis = new FileInputStream("src/test/resources/bla.7z");
    	try {
            InputStream bis = new BufferedInputStream(fis);
            try {
                new ArchiveStreamFactory().createArchiveInputStream(bis);
                fail("Expected a StreamingNotSupportedException");
            } catch (StreamingNotSupportedException ex) {
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
    	FileInputStream fis = new FileInputStream("src/test/resources/COMPRESS-208.zip");
    	try {
            InputStream bis = new BufferedInputStream(fis);
            try {
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(bis);
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

// org.apache.commons.compress.archivers.CpioTestCase::testCpioArchiveCreation
    public void testCpioArchiveCreation() throws Exception {
        final File output = new File(dir, "bla.cpio");

        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");

        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("cpio", out);
        os.putArchiveEntry(new CpioArchiveEntry("test1.xml", file1.length()));
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();

        os.putArchiveEntry(new CpioArchiveEntry("test2.xml", file2.length()));
        IOUtils.copy(new FileInputStream(file2), os);
        os.closeArchiveEntry();

        os.close();
        out.close();
    }

// org.apache.commons.compress.archivers.CpioTestCase::testCpioUnarchive
    public void testCpioUnarchive() throws Exception {
        final File output = new File(dir, "bla.cpio");
        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");
        final long file1Length = file1.length();
        final long file2Length = file2.length();

        {
            final OutputStream out = new FileOutputStream(output);
            final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("cpio", out);
            CpioArchiveEntry entry = new CpioArchiveEntry("test1.xml", file1Length);
            entry.setMode(CpioConstants.C_ISREG);
            os.putArchiveEntry(entry);
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            entry = new CpioArchiveEntry("test2.xml", file2Length);
            entry.setMode(CpioConstants.C_ISREG);
            os.putArchiveEntry(entry);
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();
            os.finish();
            os.close();
            out.close();
        }

        
        final File input = output;
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("cpio", is);

        Map<String, File> result = new HashMap<String, File>();
        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            File cpioget = new File(dir, entry.getName());
            final OutputStream out = new FileOutputStream(cpioget);
            IOUtils.copy(in, out);
            out.close();
            result.put(entry.getName(), cpioget);
        }
        in.close();
        is.close();

        File t = result.get("test1.xml");
        assertTrue("Expected " + t.getAbsolutePath() + " to exist", t.exists());
        assertEquals("length of " + t.getAbsolutePath(), file1Length, t.length());

        t = result.get("test2.xml");
        assertTrue("Expected " + t.getAbsolutePath() + " to exist", t.exists());
        assertEquals("length of " + t.getAbsolutePath(), file2Length, t.length());
    }

// org.apache.commons.compress.archivers.CpioTestCase::testDirectoryEntryFromFile
    public void testDirectoryEntryFromFile() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        CpioArchiveOutputStream tos = null;
        CpioArchiveInputStream tis = null;
        try {
            archive = File.createTempFile("test.", ".cpio", tmp[0]);
            archive.deleteOnExit();
            tos = new CpioArchiveOutputStream(new FileOutputStream(archive));
            long beforeArchiveWrite = tmp[0].lastModified();
            CpioArchiveEntry in = new CpioArchiveEntry(tmp[0], "foo");
            tos.putArchiveEntry(in);
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new CpioArchiveInputStream(new FileInputStream(archive));
            CpioArchiveEntry out = tis.getNextCPIOEntry();
            tis.close();
            tis = null;
            assertNotNull(out);
            assertEquals("foo", out.getName());
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

// org.apache.commons.compress.archivers.CpioTestCase::testExplicitDirectoryEntry
    public void testExplicitDirectoryEntry() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        CpioArchiveOutputStream tos = null;
        CpioArchiveInputStream tis = null;
        try {
            archive = File.createTempFile("test.", ".cpio", tmp[0]);
            archive.deleteOnExit();
            tos = new CpioArchiveOutputStream(new FileOutputStream(archive));
            long beforeArchiveWrite = tmp[0].lastModified();
            CpioArchiveEntry in = new CpioArchiveEntry("foo/");
            in.setTime(beforeArchiveWrite / 1000);
            in.setMode(CpioConstants.C_ISDIR);
            tos.putArchiveEntry(in);
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new CpioArchiveInputStream(new FileInputStream(archive));
            CpioArchiveEntry out = tis.getNextCPIOEntry();
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

// org.apache.commons.compress.archivers.CpioTestCase::testFileEntryFromFile
    public void testFileEntryFromFile() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        CpioArchiveOutputStream tos = null;
        CpioArchiveInputStream tis = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".cpio", tmp[0]);
            archive.deleteOnExit();
            tos = new CpioArchiveOutputStream(new FileOutputStream(archive));
            CpioArchiveEntry in = new CpioArchiveEntry(tmp[1], "foo");
            tos.putArchiveEntry(in);
            byte[] b = new byte[(int) tmp[1].length()];
            fis = new FileInputStream(tmp[1]);
            while (fis.read(b) > 0) {
                tos.write(b);
            }
            fis.close();
            fis = null;
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new CpioArchiveInputStream(new FileInputStream(archive));
            CpioArchiveEntry out = tis.getNextCPIOEntry();
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

// org.apache.commons.compress.archivers.CpioTestCase::testExplicitFileEntry
    public void testExplicitFileEntry() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        CpioArchiveOutputStream tos = null;
        CpioArchiveInputStream tis = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".cpio", tmp[0]);
            archive.deleteOnExit();
            tos = new CpioArchiveOutputStream(new FileOutputStream(archive));
            CpioArchiveEntry in = new CpioArchiveEntry("foo");
            in.setTime(tmp[1].lastModified() / 1000);
            in.setSize(tmp[1].length());
            in.setMode(CpioConstants.C_ISREG);
            tos.putArchiveEntry(in);
            byte[] b = new byte[(int) tmp[1].length()];
            fis = new FileInputStream(tmp[1]);
            while (fis.read(b) > 0) {
                tos.write(b);
            }
            fis.close();
            fis = null;
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new CpioArchiveInputStream(new FileInputStream(archive));
            CpioArchiveEntry out = tis.getNextCPIOEntry();
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

// org.apache.commons.compress.archivers.ExceptionMessageTest::testMessageWhenArchiverNameIsNull_1
    public void testMessageWhenArchiverNameIsNull_1(){
        try{
            new ArchiveStreamFactory().createArchiveInputStream(null, System.in);
            fail("Should raise an IllegalArgumentException.");
        }catch (IllegalArgumentException e) {
            Assert.assertEquals(ARCHIVER_NULL_MESSAGE, e.getMessage());
        } catch (ArchiveException e) {
            fail("ArchiveException not expected");
        }
    }

// org.apache.commons.compress.archivers.ExceptionMessageTest::testMessageWhenInputStreamIsNull
    public void testMessageWhenInputStreamIsNull(){
        try{
            new ArchiveStreamFactory().createArchiveInputStream("zip", null);
            fail("Should raise an IllegalArgumentException.");
        }catch (IllegalArgumentException e) {
            Assert.assertEquals(INPUTSTREAM_NULL_MESSAGE, e.getMessage());
        } catch (ArchiveException e) {
            fail("ArchiveException not expected");
        }
    }

// org.apache.commons.compress.archivers.ExceptionMessageTest::testMessageWhenArchiverNameIsNull_2
    public void testMessageWhenArchiverNameIsNull_2(){
        try{
            new ArchiveStreamFactory().createArchiveOutputStream(null, System.out);
            fail("Should raise an IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(ARCHIVER_NULL_MESSAGE, e.getMessage());
        } catch (ArchiveException e){
            fail("ArchiveException not expected");
        }
    }

// org.apache.commons.compress.archivers.ExceptionMessageTest::testMessageWhenOutputStreamIsNull
    public void testMessageWhenOutputStreamIsNull(){
        try{
            new ArchiveStreamFactory().createArchiveOutputStream("zip", null);
            fail("Should raise an IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(OUTPUTSTREAM_NULL_MESSAGE, e.getMessage());
        } catch (ArchiveException e) {
            fail("ArchiveException not expected");
        }
    }

// org.apache.commons.compress.archivers.JarTestCase::testJarArchiveCreation
    public void testJarArchiveCreation() throws Exception {
        final File output = new File(dir, "bla.jar");

        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");

        final OutputStream out = new FileOutputStream(output);

        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("jar", out);

        os.putArchiveEntry(new ZipArchiveEntry("testdata/test1.xml"));
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();

        os.putArchiveEntry(new ZipArchiveEntry("testdata/test2.xml"));
        IOUtils.copy(new FileInputStream(file2), os);
        os.closeArchiveEntry();

        os.close();
    }

// org.apache.commons.compress.archivers.JarTestCase::testJarUnarchive
    public void testJarUnarchive() throws Exception {
        final File input = getFile("bla.jar");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("jar", is);

        ZipArchiveEntry entry = (ZipArchiveEntry)in.getNextEntry();
        File o = new File(dir, entry.getName());
        o.getParentFile().mkdirs();
        OutputStream out = new FileOutputStream(o);
        IOUtils.copy(in, out);
        out.close();

        entry = (ZipArchiveEntry)in.getNextEntry();
        o = new File(dir, entry.getName());
        o.getParentFile().mkdirs();
        out = new FileOutputStream(o);
        IOUtils.copy(in, out);
        out.close();

        entry = (ZipArchiveEntry)in.getNextEntry();
        o = new File(dir, entry.getName());
        o.getParentFile().mkdirs();
        out = new FileOutputStream(o);
        IOUtils.copy(in, out);
        out.close();

        in.close();
        is.close();
    }

// org.apache.commons.compress.archivers.JarTestCase::testJarUnarchiveAll
    public void testJarUnarchiveAll() throws Exception {
        final File input = getFile("bla.jar");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("jar", is);

        ArchiveEntry entry = in.getNextEntry();
        while (entry != null) {
            File archiveEntry = new File(dir, entry.getName());
            archiveEntry.getParentFile().mkdirs();
            if(entry.isDirectory()){
                archiveEntry.mkdir();
                entry = in.getNextEntry();
                continue;
            }
            OutputStream out = new FileOutputStream(archiveEntry);
            IOUtils.copy(in, out);
            out.close();
            entry = in.getNextEntry();
        }

        in.close();
        is.close();
    }

// org.apache.commons.compress.archivers.LongPathTest::testArchive
    public void testArchive() {}

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
        String name = "testdata/12345678901234567890123456789012345678901234567890123456789012345678901234567890123456.xml";
        byte[] bytes = name.getBytes(CharsetNames.UTF_8);
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
        FileInputStream in = new FileInputStream(file1);
        IOUtils.copy(in, os);
        os.closeArchiveEntry();
        os.close();
        out.close();
        in.close();

        ArchiveOutputStream os2 = null;
        try {
            String toLongName = "testdata/123456789012345678901234567890123456789012345678901234567890123456789012345678901234567.xml";
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
        } catch(IOException e) {
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
        File[] tmp = createTempDirAndFile();
        File archive = null;
        TarArchiveOutputStream tos = null;
        TarArchiveInputStream tis = null;
        try {
            archive = File.createTempFile("test.", ".tar", tmp[0]);
            archive.deleteOnExit();
            tos = new TarArchiveOutputStream(new FileOutputStream(archive));
            long beforeArchiveWrite = tmp[0].lastModified();
            TarArchiveEntry in = new TarArchiveEntry(tmp[0], "foo");
            tos.putArchiveEntry(in);
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new TarArchiveInputStream(new FileInputStream(archive));
            TarArchiveEntry out = tis.getNextTarEntry();
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
        File[] tmp = createTempDirAndFile();
        File archive = null;
        TarArchiveOutputStream tos = null;
        TarArchiveInputStream tis = null;
        try {
            archive = File.createTempFile("test.", ".tar", tmp[0]);
            archive.deleteOnExit();
            tos = new TarArchiveOutputStream(new FileOutputStream(archive));
            long beforeArchiveWrite = tmp[0].lastModified();
            TarArchiveEntry in = new TarArchiveEntry("foo/");
            in.setModTime(beforeArchiveWrite);
            tos.putArchiveEntry(in);
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new TarArchiveInputStream(new FileInputStream(archive));
            TarArchiveEntry out = tis.getNextTarEntry();
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
        File[] tmp = createTempDirAndFile();
        File archive = null;
        TarArchiveOutputStream tos = null;
        TarArchiveInputStream tis = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".tar", tmp[0]);
            archive.deleteOnExit();
            tos = new TarArchiveOutputStream(new FileOutputStream(archive));
            TarArchiveEntry in = new TarArchiveEntry(tmp[1], "foo");
            tos.putArchiveEntry(in);
            byte[] b = new byte[(int) tmp[1].length()];
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
            TarArchiveEntry out = tis.getNextTarEntry();
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
        File[] tmp = createTempDirAndFile();
        File archive = null;
        TarArchiveOutputStream tos = null;
        TarArchiveInputStream tis = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".tar", tmp[0]);
            archive.deleteOnExit();
            tos = new TarArchiveOutputStream(new FileOutputStream(archive));
            TarArchiveEntry in = new TarArchiveEntry("foo");
            in.setModTime(tmp[1].lastModified());
            in.setSize(tmp[1].length());
            tos.putArchiveEntry(in);
            byte[] b = new byte[(int) tmp[1].length()];
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
            TarArchiveEntry out = tis.getNextTarEntry();
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
        } catch (IOException e) {
            Throwable t = e.getCause();
            assertTrue("Expected cause = IllegalArgumentException", t instanceof IllegalArgumentException);
        }
        in.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testZipArchiveCreation
    public void testZipArchiveCreation() throws Exception {
        
        final File output = new File(dir, "bla.zip");
        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");

        final OutputStream out = new FileOutputStream(output);
        ArchiveOutputStream os = null;
        try {
            os = new ArchiveStreamFactory()
                .createArchiveOutputStream("zip", out);
            os.putArchiveEntry(new ZipArchiveEntry("testdata/test1.xml"));
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ZipArchiveEntry("testdata/test2.xml"));
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();
        } finally {
            if (os != null) {
                os.close();
            }
        }
        out.close();

        
        List<File> results = new ArrayList<File>();

        final InputStream is = new FileInputStream(output);
        ArchiveInputStream in = null;
        try {
            in = new ArchiveStreamFactory()
                .createArchiveInputStream("zip", is);

            ZipArchiveEntry entry = null;
            while((entry = (ZipArchiveEntry)in.getNextEntry()) != null) {
                File outfile = new File(resultDir.getCanonicalPath() + "/result/" + entry.getName());
                outfile.getParentFile().mkdirs();
                OutputStream o = new FileOutputStream(outfile);
                try {
                    IOUtils.copy(in, o);
                } finally {
                    o.close();
                }
                results.add(outfile);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        is.close();

        assertEquals(results.size(), 2);
        File result = results.get(0);
        assertEquals(file1.length(), result.length());
        result = results.get(1);
        assertEquals(file2.length(), result.length());
    }

// org.apache.commons.compress.archivers.ZipTestCase::testZipUnarchive
    public void testZipUnarchive() throws Exception {
        final File input = getFile("bla.zip");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("zip", is);
        final ZipArchiveEntry entry = (ZipArchiveEntry)in.getNextEntry();
        final OutputStream out = new FileOutputStream(new File(dir, entry.getName()));
        IOUtils.copy(in, out);
        out.close();
        in.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testSkipsPK00Prefix
    public void testSkipsPK00Prefix() throws Exception {
        final File input = getFile("COMPRESS-208.zip");
        InputStream is = new FileInputStream(input);
        ArrayList<String> al = new ArrayList<String>();
        al.add("test1.xml");
        al.add("test2.xml");
        try {
            checkArchiveContent(new ZipArchiveInputStream(is), al);
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.archivers.ZipTestCase::testSupportedCompressionMethod
    public void testSupportedCompressionMethod() throws IOException {
        
        
        ZipFile moby = new ZipFile(getFile("moby.zip"));
        ZipArchiveEntry entry = moby.getEntry("README");
        assertEquals("method", ZipMethod.TOKENIZATION.getCode(), entry.getMethod());
        assertFalse(moby.canReadEntryData(entry));
        moby.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testSkipEntryWithUnsupportedCompressionMethod
    public void testSkipEntryWithUnsupportedCompressionMethod()
            throws IOException {
        ZipArchiveInputStream zip =
            new ZipArchiveInputStream(new FileInputStream(getFile("moby.zip")));
        try {
            ZipArchiveEntry entry = zip.getNextZipEntry();
            assertEquals("method", ZipMethod.TOKENIZATION.getCode(), entry.getMethod());
            assertEquals("README", entry.getName());
            assertFalse(zip.canReadEntryData(entry));
            try {
                assertNull(zip.getNextZipEntry());
            } catch (IOException e) {
                e.printStackTrace();
                fail("COMPRESS-93: Unable to skip an unsupported zip entry");
            }
        } finally {
            zip.close();
        }
    }

// org.apache.commons.compress.archivers.ZipTestCase::testListAllFilesWithNestedArchive
    public void testListAllFilesWithNestedArchive() throws Exception {
        final File input = getFile("OSX_ArchiveWithNestedArchive.zip");

        List<String> results = new ArrayList<String>();

        final InputStream is = new FileInputStream(input);
        ArchiveInputStream in = null;
        try {
            in = new ArchiveStreamFactory().createArchiveInputStream("zip", is);

            ZipArchiveEntry entry = null;
            while((entry = (ZipArchiveEntry)in.getNextEntry()) != null) {
                results.add(entry.getName());

                ArchiveInputStream nestedIn = new ArchiveStreamFactory().createArchiveInputStream("zip", in);
                ZipArchiveEntry nestedEntry = null;
                while((nestedEntry = (ZipArchiveEntry)nestedIn.getNextEntry()) != null) {
                    results.add(nestedEntry.getName());
                }
               
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        is.close();

        results.contains("NestedArchiv.zip");
        results.contains("test1.xml");
        results.contains("test2.xml");
        results.contains("test3.xml");
    }

// org.apache.commons.compress.archivers.ZipTestCase::testDirectoryEntryFromFile
    public void testDirectoryEntryFromFile() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        ZipArchiveOutputStream zos = null;
        ZipFile zf = null;
        try {
            archive = File.createTempFile("test.", ".zip", tmp[0]);
            archive.deleteOnExit();
            zos = new ZipArchiveOutputStream(archive);
            long beforeArchiveWrite = tmp[0].lastModified();
            ZipArchiveEntry in = new ZipArchiveEntry(tmp[0], "foo");
            zos.putArchiveEntry(in);
            zos.closeArchiveEntry();
            zos.close();
            zos = null;
            zf = new ZipFile(archive);
            ZipArchiveEntry out = zf.getEntry("foo/");
            assertNotNull(out);
            assertEquals("foo/", out.getName());
            assertEquals(0, out.getSize());
            
            assertEquals(beforeArchiveWrite / 2000,
                         out.getLastModifiedDate().getTime() / 2000);
            assertTrue(out.isDirectory());
        } finally {
            ZipFile.closeQuietly(zf);
            if (zos != null) {
                zos.close();
            }
            tryHardToDelete(archive);
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.ZipTestCase::testExplicitDirectoryEntry
    public void testExplicitDirectoryEntry() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        ZipArchiveOutputStream zos = null;
        ZipFile zf = null;
        try {
            archive = File.createTempFile("test.", ".zip", tmp[0]);
            archive.deleteOnExit();
            zos = new ZipArchiveOutputStream(archive);
            long beforeArchiveWrite = tmp[0].lastModified();
            ZipArchiveEntry in = new ZipArchiveEntry("foo/");
            in.setTime(beforeArchiveWrite);
            zos.putArchiveEntry(in);
            zos.closeArchiveEntry();
            zos.close();
            zos = null;
            zf = new ZipFile(archive);
            ZipArchiveEntry out = zf.getEntry("foo/");
            assertNotNull(out);
            assertEquals("foo/", out.getName());
            assertEquals(0, out.getSize());
            assertEquals(beforeArchiveWrite / 2000,
                         out.getLastModifiedDate().getTime() / 2000);
            assertTrue(out.isDirectory());
        } finally {
            ZipFile.closeQuietly(zf);
            if (zos != null) {
                zos.close();
            }
            tryHardToDelete(archive);
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.ZipTestCase::testFileEntryFromFile
    public void testFileEntryFromFile() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        ZipArchiveOutputStream zos = null;
        ZipFile zf = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".zip", tmp[0]);
            archive.deleteOnExit();
            zos = new ZipArchiveOutputStream(archive);
            ZipArchiveEntry in = new ZipArchiveEntry(tmp[1], "foo");
            zos.putArchiveEntry(in);
            byte[] b = new byte[(int) tmp[1].length()];
            fis = new FileInputStream(tmp[1]);
            while (fis.read(b) > 0) {
                zos.write(b);
            }
            fis.close();
            fis = null;
            zos.closeArchiveEntry();
            zos.close();
            zos = null;
            zf = new ZipFile(archive);
            ZipArchiveEntry out = zf.getEntry("foo");
            assertNotNull(out);
            assertEquals("foo", out.getName());
            assertEquals(tmp[1].length(), out.getSize());
            assertEquals(tmp[1].lastModified() / 2000,
                         out.getLastModifiedDate().getTime() / 2000);
            assertFalse(out.isDirectory());
        } finally {
            ZipFile.closeQuietly(zf);
            if (zos != null) {
                zos.close();
            }
            tryHardToDelete(archive);
            if (fis != null) {
                fis.close();
            }
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.ZipTestCase::testExplicitFileEntry
    public void testExplicitFileEntry() throws Exception {
        File[] tmp = createTempDirAndFile();
        File archive = null;
        ZipArchiveOutputStream zos = null;
        ZipFile zf = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".zip", tmp[0]);
            archive.deleteOnExit();
            zos = new ZipArchiveOutputStream(archive);
            ZipArchiveEntry in = new ZipArchiveEntry("foo");
            in.setTime(tmp[1].lastModified());
            in.setSize(tmp[1].length());
            zos.putArchiveEntry(in);
            byte[] b = new byte[(int) tmp[1].length()];
            fis = new FileInputStream(tmp[1]);
            while (fis.read(b) > 0) {
                zos.write(b);
            }
            fis.close();
            fis = null;
            zos.closeArchiveEntry();
            zos.close();
            zos = null;
            zf = new ZipFile(archive);
            ZipArchiveEntry out = zf.getEntry("foo");
            assertNotNull(out);
            assertEquals("foo", out.getName());
            assertEquals(tmp[1].length(), out.getSize());
            assertEquals(tmp[1].lastModified() / 2000,
                         out.getLastModifiedDate().getTime() / 2000);
            assertFalse(out.isDirectory());
        } finally {
            ZipFile.closeQuietly(zf);
            if (zos != null) {
                zos.close();
            }
            tryHardToDelete(archive);
            if (fis != null) {
                fis.close();
            }
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.ar.ArArchiveInputStreamTest::testReadLongNamesGNU
    public void testReadLongNamesGNU() throws Exception {
        checkLongNameEntry("longfile_gnu.ar");
    }

// org.apache.commons.compress.archivers.ar.ArArchiveInputStreamTest::testReadLongNamesBSD
    public void testReadLongNamesBSD() throws Exception {
        checkLongNameEntry("longfile_bsd.ar");
    }

// org.apache.commons.compress.archivers.ar.ArArchiveOutputStreamTest::testLongFileNamesCauseExceptionByDefault
    public void testLongFileNamesCauseExceptionByDefault() {
        ArArchiveOutputStream os = null;
        try {
            os = new ArArchiveOutputStream(new ByteArrayOutputStream());
            ArArchiveEntry ae = new ArArchiveEntry("this_is_a_long_name.txt",
                                                   0);
            os.putArchiveEntry(ae);
            fail("Expected an exception");
        } catch (IOException ex) {
            assertTrue(ex.getMessage().startsWith("filename too long"));
        } finally {
            closeQuietly(os);
        }
    }

// org.apache.commons.compress.archivers.ar.ArArchiveOutputStreamTest::testLongFileNamesWorkUsingBSDDialect
    public void testLongFileNamesWorkUsingBSDDialect() throws Exception {
        FileOutputStream fos = null;
        ArArchiveOutputStream os = null;
        File[] df = createTempDirAndFile();
        try {
            fos = new FileOutputStream(df[1]);
            os = new ArArchiveOutputStream(fos);
            os.setLongFileMode(ArArchiveOutputStream.LONGFILE_BSD);
            ArArchiveEntry ae = new ArArchiveEntry("this_is_a_long_name.txt",
                                                   14);
            os.putArchiveEntry(ae);
            os.write(new byte[] {
                    'H', 'e', 'l', 'l', 'o', ',', ' ',
                    'w', 'o', 'r', 'l', 'd', '!', '\n'
                });
            os.closeArchiveEntry();
            os.close();
            os = null;
            fos = null;

            List<String> expected = new ArrayList<String>();
            expected.add("this_is_a_long_name.txt");
            checkArchiveContent(df[1], expected);
        } finally {
            if (os != null) {
                os.close();
            }
            if (fos != null) {
                fos.close();
            }
            rmdir(df[0]);
        }
    }

// org.apache.commons.compress.archivers.arj.ArjArchiveInputStreamTest::testArjUnarchive
    public void testArjUnarchive() throws Exception {
        StringBuilder expected = new StringBuilder();
        expected.append("test1.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>test2.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>\n");

        ArjArchiveInputStream in = new ArjArchiveInputStream(new FileInputStream(getFile("bla.arj")));
        ArjArchiveEntry entry;

        StringBuilder result = new StringBuilder();
        while ((entry = in.getNextEntry()) != null) {
            result.append(entry.getName());
            int tmp;
            while ((tmp = in.read()) != -1) {
                result.append((char) tmp);
            }
            assertFalse(entry.isDirectory());
        }
        in.close();
        assertEquals(result.toString(), expected.toString());
    }

// org.apache.commons.compress.archivers.arj.ArjArchiveInputStreamTest::testReadingOfAttributesDosVersion
    public void testReadingOfAttributesDosVersion() throws Exception {
        ArjArchiveInputStream in = new ArjArchiveInputStream(new FileInputStream(getFile("bla.arj")));
        ArjArchiveEntry entry = in.getNextEntry();
        assertEquals("test1.xml", entry.getName());
        assertEquals(30, entry.getSize());
        assertEquals(0, entry.getUnixMode());
        Calendar cal = Calendar.getInstance();
        cal.set(2008, 9, 6, 23, 50, 52);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), entry.getLastModifiedDate());
    }

// org.apache.commons.compress.archivers.arj.ArjArchiveInputStreamTest::testReadingOfAttributesUnixVersion
    public void testReadingOfAttributesUnixVersion() throws Exception {
        ArjArchiveInputStream in = new ArjArchiveInputStream(new FileInputStream(getFile("bla.unix.arj")));
        ArjArchiveEntry entry = in.getNextEntry();
        assertEquals("test1.xml", entry.getName());
        assertEquals(30, entry.getSize());
        assertEquals(0664, entry.getUnixMode() & 07777 );
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0000"));
        cal.set(2008, 9, 6, 21, 50, 52);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), entry.getLastModifiedDate());
    }

// org.apache.commons.compress.archivers.cpio.CpioArchiveInputStreamTest::testCpioUnarchive
    public void testCpioUnarchive() throws Exception {
        StringBuilder expected = new StringBuilder();
        expected.append("./test1.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>./test2.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>\n");

        CpioArchiveInputStream in = new CpioArchiveInputStream(new FileInputStream(getFile("bla.cpio")));
        CpioArchiveEntry entry;

        StringBuilder result = new StringBuilder();
        while ((entry = (CpioArchiveEntry) in.getNextEntry()) != null) {
            result.append(entry.getName());
            int tmp;
            while ((tmp = in.read()) != -1) {
                result.append((char) tmp);
            }
        }
        in.close();
        assertEquals(result.toString(), expected.toString());
    }

// org.apache.commons.compress.archivers.cpio.CpioArchiveInputStreamTest::testCpioUnarchiveCreatedByRedlineRpm
    public void testCpioUnarchiveCreatedByRedlineRpm() throws Exception {
        CpioArchiveInputStream in =
            new CpioArchiveInputStream(new FileInputStream(getFile("redline.cpio")));
        CpioArchiveEntry entry= null;

        int count = 0;
        while ((entry = (CpioArchiveEntry) in.getNextEntry()) != null) {
            count++;
        }
        in.close();

        assertEquals(count, 1);
    }

// org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStreamTest::testWriteOldBinary
    public void testWriteOldBinary() throws Exception {
        final File f = getFile("test1.xml");
        final File output = new File(dir, "test.cpio");
        final FileOutputStream out = new FileOutputStream(output);
        InputStream in = null;
        try {
            final CpioArchiveOutputStream os =
                new CpioArchiveOutputStream(out, CpioConstants
                                            .FORMAT_OLD_BINARY);
            os.putArchiveEntry(new CpioArchiveEntry(CpioConstants
                                                    .FORMAT_OLD_BINARY,
                                                    f, "test1.xml"));
            IOUtils.copy(in = new FileInputStream(f), os);
            in.close();
            in = null;
            os.closeArchiveEntry();
            os.close();
        } finally {
            if (in != null) {
                in.close();
            }
            out.close();
        }

        try {
            in = new CpioArchiveInputStream(new FileInputStream(output));
            CpioArchiveEntry e = ((CpioArchiveInputStream) in)
                .getNextCPIOEntry();
            assertEquals("test1.xml", e.getName());
            assertNull(((CpioArchiveInputStream) in).getNextEntry());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

// org.apache.commons.compress.archivers.dump.DumpArchiveInputStreamTest::testNotADumpArchive
    public void testNotADumpArchive() throws Exception {
        FileInputStream is = new FileInputStream(getFile("bla.zip"));
        try {
            new DumpArchiveInputStream(is);
            fail("expected an exception");
        } catch (ArchiveException ex) {
            
            assertTrue(ex.getCause() instanceof ShortFileException);
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.archivers.dump.DumpArchiveInputStreamTest::testNotADumpArchiveButBigEnough
    public void testNotADumpArchiveButBigEnough() throws Exception {
        FileInputStream is = new FileInputStream(getFile("zip64support.tar.bz2"));
        try {
            new DumpArchiveInputStream(is);
            fail("expected an exception");
        } catch (ArchiveException ex) {
            
            assertTrue(ex.getCause() instanceof UnrecognizedFormatException);
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.archivers.dump.DumpArchiveInputStreamTest::testConsumesArchiveCompletely
    public void testConsumesArchiveCompletely() throws Exception {
        InputStream is = DumpArchiveInputStreamTest.class
            .getResourceAsStream("/archive_with_trailer.dump");
        DumpArchiveInputStream dump = new DumpArchiveInputStream(is);
        while (dump.getNextDumpEntry() != null) {
            
        }
        byte[] expected = new byte[] {
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', '\n'
        };
        byte[] actual = new byte[expected.length];
        is.read(actual);
        assertArrayEquals(expected, actual);
        dump.close();
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
        } catch (IOException ex) {
            assertEquals("Cannot read encrypted files without a password",
                         ex.getMessage());
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

// org.apache.commons.compress.archivers.tar.SparseFilesTest::testOldGNU
    public void testOldGNU() throws Throwable {
        File file = getFile("oldgnu_sparse.tar");
        TarArchiveInputStream tin = null;
        try {
            tin = new TarArchiveInputStream(new FileInputStream(file));
            TarArchiveEntry ae = tin.getNextTarEntry();
            assertEquals("sparsefile", ae.getName());
            assertTrue(ae.isGNUSparse());
            assertFalse(tin.canReadEntryData(ae));
        } finally {
            if (tin != null) {
                tin.close();
            }
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testFileSystemRoot
    public void testFileSystemRoot() {
        TarArchiveEntry t = new TarArchiveEntry(new File(ROOT));
        assertEquals("/", t.getName());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testTarFileWithFSRoot
    public void testTarFileWithFSRoot() throws IOException {
        File f = File.createTempFile("taetest", ".tar");
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
        TarArchiveEntry t = new TarArchiveEntry("");
        t.setSize(0);
        t.setSize(1);
        try {
            t.setSize(-1);
            fail("Should have generated IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        t.setSize(077777777777L);
        t.setSize(0100000000000L);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testLinkFlagConstructor
    public void testLinkFlagConstructor() {
        TarArchiveEntry t = new TarArchiveEntry("/foo", LF_GNUTYPE_LONGNAME);
        assertGnuMagic(t);
        assertEquals("foo", t.getName());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testLinkFlagConstructorWithFileFlag
    public void testLinkFlagConstructorWithFileFlag() {
        TarArchiveEntry t = new TarArchiveEntry("/foo", LF_NORMAL);
        assertPosixMagic(t);
        assertEquals("foo", t.getName());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveEntryTest::testLinkFlagConstructorWithPreserve
    public void testLinkFlagConstructorWithPreserve() {
        TarArchiveEntry t = new TarArchiveEntry("/foo", LF_GNUTYPE_LONGNAME,
                                                true);
        assertGnuMagic(t);
        assertEquals("/foo", t.getName());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readSimplePaxHeader
    public void readSimplePaxHeader() throws Exception {
        final InputStream is = new ByteArrayInputStream(new byte[1]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(is);
        Map<String, String> headers = tais
            .parsePaxHeaders(new ByteArrayInputStream("30 atime=1321711775.972059463\n"
                                                      .getBytes(CharsetNames.UTF_8)));
        assertEquals(1, headers.size());
        assertEquals("1321711775.972059463", headers.get("atime"));
        tais.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readPaxHeaderWithEmbeddedNewline
    public void readPaxHeaderWithEmbeddedNewline() throws Exception {
        final InputStream is = new ByteArrayInputStream(new byte[1]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(is);
        Map<String, String> headers = tais
            .parsePaxHeaders(new ByteArrayInputStream("28 comment=line1\nline2\nand3\n"
                                                      .getBytes(CharsetNames.UTF_8)));
        assertEquals(1, headers.size());
        assertEquals("line1\nline2\nand3", headers.get("comment"));
        tais.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readNonAsciiPaxHeader
    public void readNonAsciiPaxHeader() throws Exception {
        String ae = "\u00e4";
        String line = "11 path="+ ae + "\n";
        assertEquals(11, line.getBytes(CharsetNames.UTF_8).length);
        final InputStream is = new ByteArrayInputStream(new byte[1]);
        final TarArchiveInputStream tais = new TarArchiveInputStream(is);
        Map<String, String> headers = tais
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
        TarArchiveInputStream tar = getTestStream("/COMPRESS-197.tar");
        try {
            TarArchiveEntry entry = tar.getNextTarEntry();
            while (entry != null) {
                entry = tar.getNextTarEntry();
            }
        } catch (IOException e) {
            fail("COMPRESS-197: " + e.getMessage());
        } finally {
            tar.close();
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::shouldUseSpecifiedEncodingWhenReadingGNULongNames
    public void shouldUseSpecifiedEncodingWhenReadingGNULongNames()
        throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String encoding = CharsetNames.UTF_16;
        String name = "1234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890\u00e4";
        TarArchiveOutputStream tos =
            new TarArchiveOutputStream(bos, encoding);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        TarArchiveEntry t = new TarArchiveEntry(name);
        t.setSize(1);
        tos.putArchiveEntry(t);
        tos.write(30);
        tos.closeArchiveEntry();
        tos.close();
        byte[] data = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        TarArchiveInputStream tis =
            new TarArchiveInputStream(bis, encoding);
        t = tis.getNextTarEntry();
        assertEquals(name, t.getName());
        tis.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::shouldConsumeArchiveCompletely
    public void shouldConsumeArchiveCompletely() throws Exception {
        InputStream is = TarArchiveInputStreamTest.class
            .getResourceAsStream("/archive_with_trailer.tar");
        TarArchiveInputStream tar = new TarArchiveInputStream(is);
        while (tar.getNextTarEntry() != null) {
            
        }
        byte[] expected = new byte[] {
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', '\n'
        };
        byte[] actual = new byte[expected.length];
        is.read(actual);
        assertArrayEquals(expected, actual);
        tar.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readsArchiveCompletely_COMPRESS245
    public void readsArchiveCompletely_COMPRESS245() throws Exception {
        InputStream is = TarArchiveInputStreamTest.class
            .getResourceAsStream("/COMPRESS-245.tar.gz");
        try {
            InputStream gin = new GZIPInputStream(is);
            TarArchiveInputStream tar = new TarArchiveInputStream(gin);
            int count = 0;
            TarArchiveEntry entry = tar.getNextTarEntry();
            while (entry != null) {
                count++;
                entry = tar.getNextTarEntry();
            }
            assertEquals(31, count);
        } catch (IOException e) {
            fail("COMPRESS-245: " + e.getMessage());
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::shouldThrowAnExceptionOnTruncatedEntries
    public void shouldThrowAnExceptionOnTruncatedEntries() throws Exception {
        File dir = mkdir("COMPRESS-279");
        TarArchiveInputStream is = getTestStream("/COMPRESS-279.tar");
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
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testCount
    public void testCount() throws Exception {
        File f = File.createTempFile("commons-compress-tarcount", ".tar");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);

        ArchiveOutputStream tarOut = new ArchiveStreamFactory()
            .createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);

        File file1 = getFile("test1.xml");
        TarArchiveEntry sEntry = new TarArchiveEntry(file1, file1.getName());
        tarOut.putArchiveEntry(sEntry);

        FileInputStream in = new FileInputStream(file1);
        byte[] buf = new byte[8192];

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
        TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(077777777777L);
        TarArchiveOutputStream tos =
            new TarArchiveOutputStream(new ByteArrayOutputStream());
        tos.putArchiveEntry(t);
        t.setSize(0100000000000L);
        tos = new TarArchiveOutputStream(new ByteArrayOutputStream());
        try {
            tos.putArchiveEntry(t);
            fail("Should have generated RuntimeException");
        } catch (RuntimeException expected) {
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testBigNumberStarMode
    public void testBigNumberStarMode() throws Exception {
        TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(0100000000000L);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        byte[] data = bos.toByteArray();
        assertEquals(0x80,
                     data[TarConstants.NAMELEN
                        + TarConstants.MODELEN
                        + TarConstants.UIDLEN
                        + TarConstants.GIDLEN] & 0x80);
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(0100000000000L, e.getSize());
        tin.close();
        
        
        closeQuietly(tos);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testBigNumberPosixMode
    public void testBigNumberPosixMode() throws Exception {
        TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(0100000000000L);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        byte[] data = bos.toByteArray();
        assertEquals("00000000000 ",
                     new String(data,
                                1024 + TarConstants.NAMELEN
                                + TarConstants.MODELEN
                                + TarConstants.UIDLEN
                                + TarConstants.GIDLEN, 12,
                                CharsetNames.UTF_8));
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(0100000000000L, e.getSize());
        tin.close();
        
        
        closeQuietly(tos);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteSimplePaxHeaders
    public void testWriteSimplePaxHeaders() throws Exception {
        Map<String, String> m = new HashMap<String, String>();
        m.put("a", "b");
        byte[] data = writePaxHeader(m);
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
        Map<String, String> m = new HashMap<String, String>();
        m.put("a",
              "0123456789012345678901234567890123456789"
              + "01234567890123456789012345678901234567890123456789"
              + "012");
        byte[] data = writePaxHeader(m);
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
        Map<String, String> m = new HashMap<String, String>();
        m.put("a",
              "0123456789012345678901234567890123456789"
              + "01234567890123456789012345678901234567890123456789"
              + "0123");
        byte[] data = writePaxHeader(m);
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
        String n = "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789";
        TarArchiveEntry t =
            new TarArchiveEntry(n);
        t.setSize(10 * 1024);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tos.putArchiveEntry(t);
        tos.write(new byte[10 * 1024]);
        tos.closeArchiveEntry();
        byte[] data = bos.toByteArray();
        assertEquals("160 path=" + n + "\n",
                     new String(data, 512, 160, CharsetNames.UTF_8));
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        tin.close();
        tos.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testOldEntryStarMode
    public void testOldEntryStarMode() throws Exception {
        TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(Integer.MAX_VALUE);
        t.setModTime(-1000);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        byte[] data = bos.toByteArray();
        assertEquals((byte) 0xff,
                     data[TarConstants.NAMELEN
                          + TarConstants.MODELEN
                          + TarConstants.UIDLEN
                          + TarConstants.GIDLEN
                          + TarConstants.SIZELEN]);
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1969, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), e.getLastModifiedDate());
        tin.close();
        
        
        closeQuietly(tos);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testOldEntryPosixMode
    public void testOldEntryPosixMode() throws Exception {
        TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(Integer.MAX_VALUE);
        t.setModTime(-1000);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        byte[] data = bos.toByteArray();
        assertEquals("00000000000 ",
                     new String(data,
                                1024 + TarConstants.NAMELEN
                                + TarConstants.MODELEN
                                + TarConstants.UIDLEN
                                + TarConstants.GIDLEN
                                + TarConstants.SIZELEN, 12,
                                CharsetNames.UTF_8));
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(1969, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), e.getLastModifiedDate());
        tin.close();
        
        
        closeQuietly(tos);
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testOldEntryError
    public void testOldEntryError() throws Exception {
        TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(Integer.MAX_VALUE);
        t.setModTime(-1000);
        TarArchiveOutputStream tos =
            new TarArchiveOutputStream(new ByteArrayOutputStream());
        try {
            tos.putArchiveEntry(t);
            fail("Should have generated RuntimeException");
        } catch (RuntimeException expected) {
        }
        tos.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteNonAsciiPathNamePaxHeader
    public void testWriteNonAsciiPathNamePaxHeader() throws Exception {
        String n = "\u00e4";
        TarArchiveEntry t = new TarArchiveEntry(n);
        t.setSize(10 * 1024);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.write(new byte[10 * 1024]);
        tos.closeArchiveEntry();
        tos.close();
        byte[] data = bos.toByteArray();
        assertEquals("11 path=" + n + "\n",
                     new String(data, 512, 11, CharsetNames.UTF_8));
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteNonAsciiLinkPathNamePaxHeader
    public void testWriteNonAsciiLinkPathNamePaxHeader() throws Exception {
        String n = "\u00e4";
        TarArchiveEntry t = new TarArchiveEntry("a", TarConstants.LF_LINK);
        t.setSize(10 * 1024);
        t.setLinkName(n);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.write(new byte[10 * 1024]);
        tos.closeArchiveEntry();
        tos.close();
        byte[] data = bos.toByteArray();
        assertEquals("15 linkpath=" + n + "\n",
                     new String(data, 512, 15, CharsetNames.UTF_8));
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
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
        String n = "01234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789/";

        try {
            TarArchiveEntry t = new TarArchiveEntry(n);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
            tos.putArchiveEntry(t);
            tos.closeArchiveEntry();
            tos.close();
            
            fail("Truncated name didn't throw an exception");
        } catch (RuntimeException e) {
            
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongDirectoryNameTruncateMode
    public void testWriteLongDirectoryNameTruncateMode() throws Exception {
        String n = "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789/";
        TarArchiveEntry t = new TarArchiveEntry(n);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        tos.putArchiveEntry(t);
        tos.closeArchiveEntry();
        tos.close();
        byte[] data = bos.toByteArray();
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
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
        String n = "f\u00f6\u00f6/";
        TarArchiveEntry t = new TarArchiveEntry(n);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.closeArchiveEntry();
        tos.close();
        byte[] data = bos.toByteArray();
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        assertTrue(e.isDirectory());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteNonAsciiNameWithUnfortunateNamePosixMode
    public void testWriteNonAsciiNameWithUnfortunateNamePosixMode() throws Exception {
        String n = "f\u00f6\u00f6\u00dc";
        TarArchiveEntry t = new TarArchiveEntry(n);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.closeArchiveEntry();
        tos.close();
        byte[] data = bos.toByteArray();
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        assertFalse(e.isDirectory());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongLinkNameErrorMode
    public void testWriteLongLinkNameErrorMode() throws Exception {
        String linkname = "01234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789/test";
        TarArchiveEntry entry = new TarArchiveEntry("test", TarConstants.LF_SYMLINK);
        entry.setLinkName(linkname);
        
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
            tos.putArchiveEntry(entry);
            tos.closeArchiveEntry();
            tos.close();
            
            fail("Truncated link name didn't throw an exception");
        } catch (RuntimeException e) {
            
        }
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongLinkNameTruncateMode
    public void testWriteLongLinkNameTruncateMode() throws Exception {
        String linkname = "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789/";
        TarArchiveEntry entry = new TarArchiveEntry("test" , TarConstants.LF_SYMLINK);
        entry.setLinkName(linkname);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        
        byte[] data = bos.toByteArray();
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
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

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testWriteLongLinkName
    public void testWriteLongLinkName(int mode) throws Exception {
        String linkname = "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789"
            + "01234567890123456789012345678901234567890123456789/test";
        TarArchiveEntry entry = new TarArchiveEntry("test", TarConstants.LF_SYMLINK);
        entry.setLinkName(linkname);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, "ASCII");
        tos.setLongFileMode(mode);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        
        byte[] data = bos.toByteArray();
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals("Entry name", "test", e.getName());
        assertEquals("Link name", linkname, e.getLinkName());
        assertTrue("The entry is not a symbolic link", e.isSymbolicLink());
        tin.close();
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testPadsOutputToFullBlockLength
    public void testPadsOutputToFullBlockLength() throws Exception {
        File f = File.createTempFile("commons-compress-padding", ".tar");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
        TarArchiveOutputStream tos = new TarArchiveOutputStream(fos);
        File file1 = getFile("test1.xml");
        TarArchiveEntry sEntry = new TarArchiveEntry(file1, file1.getName());
        tos.putArchiveEntry(sEntry);
        FileInputStream in = new FileInputStream(file1);
        IOUtils.copy(in, tos);
        in.close();
        tos.closeArchiveEntry();
        tos.close();
        
        assertEquals(TarConstants.DEFAULT_BLKSIZE, f.length());
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripExplicitUnicodeExtra
    public void testUtf8FileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CharsetNames.UTF_8, true, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripNoEFSExplicitUnicodeExtra
    public void testUtf8FileRoundtripNoEFSExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CharsetNames.UTF_8, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testCP437FileRoundtripExplicitUnicodeExtra
    public void testCP437FileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CP437, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testASCIIFileRoundtripExplicitUnicodeExtra
    public void testASCIIFileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CharsetNames.US_ASCII, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripImplicitUnicodeExtra
    public void testUtf8FileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CharsetNames.UTF_8, true, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripNoEFSImplicitUnicodeExtra
    public void testUtf8FileRoundtripNoEFSImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CharsetNames.UTF_8, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testCP437FileRoundtripImplicitUnicodeExtra
    public void testCP437FileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CP437, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testASCIIFileRoundtripImplicitUnicodeExtra
    public void testASCIIFileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CharsetNames.US_ASCII, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testRead7ZipArchive
    public void testRead7ZipArchive() throws IOException {
        File archive = getFile("utf8-7zip-test.zip");
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, CP437, false);
            assertNotNull(zf.getEntry(ASCII_TXT));
            assertNotNull(zf.getEntry(EURO_FOR_DOLLAR_TXT));
            assertNotNull(zf.getEntry(OIL_BARREL_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testRead7ZipArchiveForStream
    public void testRead7ZipArchiveForStream() throws IOException {
        FileInputStream archive =
            new FileInputStream(getFile("utf8-7zip-test.zip"));
        ZipArchiveInputStream zi = null;
        try {
            zi = new ZipArchiveInputStream(archive, CP437, false);
            assertEquals(ASCII_TXT, zi.getNextEntry().getName());
            assertEquals(OIL_BARREL_TXT, zi.getNextEntry().getName());
            assertEquals(EURO_FOR_DOLLAR_TXT, zi.getNextEntry().getName());
        } finally {
            if (zi != null) {
                zi.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testReadWinZipArchive
    public void testReadWinZipArchive() throws IOException {
        File archive = getFile("utf8-winzip-test.zip");
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, null, true);
            assertCanRead(zf, ASCII_TXT);
            assertCanRead(zf, EURO_FOR_DOLLAR_TXT);
            assertCanRead(zf, OIL_BARREL_TXT);
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testReadWinZipArchiveForStream
    public void testReadWinZipArchiveForStream() throws IOException {
        FileInputStream archive =
            new FileInputStream(getFile("utf8-winzip-test.zip"));
        ZipArchiveInputStream zi = null;
        try {
            zi = new ZipArchiveInputStream(archive, null, true);
            assertEquals(EURO_FOR_DOLLAR_TXT, zi.getNextEntry().getName());
            assertEquals(OIL_BARREL_TXT, zi.getNextEntry().getName());
            assertEquals(ASCII_TXT, zi.getNextEntry().getName());
        } finally {
            if (zi != null) {
                zi.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testZipFileReadsUnicodeFields
    public void testZipFileReadsUnicodeFields() throws IOException {
        File file = File.createTempFile("unicode-test", ".zip");
        file.deleteOnExit();
        ZipArchiveInputStream zi = null;
        try {
            createTestFile(file, CharsetNames.US_ASCII, false, true);
            FileInputStream archive = new FileInputStream(file);
            zi = new ZipArchiveInputStream(archive, CharsetNames.US_ASCII, true);
            assertEquals(OIL_BARREL_TXT, zi.getNextEntry().getName());
            assertEquals(EURO_FOR_DOLLAR_TXT, zi.getNextEntry().getName());
            assertEquals(ASCII_TXT, zi.getNextEntry().getName());
        } finally {
            if (zi != null) {
                zi.close();
            }
            tryHardToDelete(file);
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testZipArchiveInputStreamReadsUnicodeFields
    public void testZipArchiveInputStreamReadsUnicodeFields()
        throws IOException {
        File file = File.createTempFile("unicode-test", ".zip");
        file.deleteOnExit();
        ZipFile zf = null;
        try {
            createTestFile(file, CharsetNames.US_ASCII, false, true);
            zf = new ZipFile(file, CharsetNames.US_ASCII, true);
            assertNotNull(zf.getEntry(ASCII_TXT));
            assertNotNull(zf.getEntry(EURO_FOR_DOLLAR_TXT));
            assertNotNull(zf.getEntry(OIL_BARREL_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
            tryHardToDelete(file);
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testRawNameReadFromZipFile
    public void testRawNameReadFromZipFile()
        throws IOException {
        File archive = getFile("utf8-7zip-test.zip");
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, CP437, false);
            assertRawNameOfAcsiiTxt(zf.getEntry(ASCII_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testRawNameReadFromStream
    public void testRawNameReadFromStream()
        throws IOException {
        FileInputStream archive =
            new FileInputStream(getFile("utf8-7zip-test.zip"));
        ZipArchiveInputStream zi = null;
        try {
            zi = new ZipArchiveInputStream(archive, CP437, false);
            assertRawNameOfAcsiiTxt((ZipArchiveEntry) zi.getNextEntry());
        } finally {
            if (zi != null) {
                zi.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8Interoperability
    public void testUtf8Interoperability() throws IOException {
        File file1 = super.getFile("utf8-7zip-test.zip");
        File file2 = super.getFile("utf8-winzip-test.zip");

        testFile(file1,CP437);
        testFile(file2,CP437);

    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddChangeTwice
    public void testAddChangeTwice() throws Exception {
        InputStream in = null;
        InputStream in2 = null;
        try {
            in = new FileInputStream(getFile("test.txt"));
            in2 = new FileInputStream(getFile("test2.xml"));

            ArchiveEntry e = new ZipArchiveEntry("test.txt");
            ArchiveEntry e2 = new ZipArchiveEntry("test.txt");

            ChangeSet changes = new ChangeSet();
            changes.add(e, in);
            changes.add(e2, in2);

            assertEquals(1, changes.getChanges().size());
            Change c = changes.getChanges().iterator().next();
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

            ArchiveEntry e = new ZipArchiveEntry("test.txt");
            ArchiveEntry e2 = new ZipArchiveEntry("test.txt");

            ChangeSet changes = new ChangeSet();
            changes.add(e, in, true);
            changes.add(e2, in2, false);

            assertEquals(1, changes.getChanges().size());
            Change c = changes.getChanges().iterator().next();
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.deleteDir("bla");
            archiveListDeleteDir("bla");
            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.deleteDir("la");
            archiveListDeleteDir("la");
            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.deleteDir("test.txt");
            archiveListDeleteDir("test.txt");
            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("bla/test5.xml");
            archiveListDelete("bla/test5.xml");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("bla");
            

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();

        File testtxt = null;
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("test/test3.xml");
            archiveListDelete("test/test3.xml");

            
            testtxt = getFile("test.txt");
            ArchiveEntry entry = out.createArchiveEntry(testtxt, "test/test3.xml");
            changes.add(entry, new FileInputStream(testtxt));
            archiveList.add("test/test3.xml");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
            File test3xml = new File(check,"result/test/test3.xml");
            assertEquals(testtxt.length(), test3xml.length());

            BufferedReader reader = new BufferedReader(new FileReader(test3xml));
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.deleteDir("bla");
            archiveListDeleteDir("bla");

            
            final File file1 = getFile("test.txt");
            ArchiveEntry entry = out.createArchiveEntry(file1, "bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            ChangeSetResults results = performer.perform(ais, out);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.deleteDir("bla");
            archiveListDeleteDir("bla");

            
            final File file1 = getFile("test.txt");
            ArchiveEntry entry = out.createArchiveEntry(file1, "bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("blub/test.txt");

            changes.delete("testdata/test1.xml");
            archiveListDelete("testdata/test1.xml");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ZipFile ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            ais = new ZipFile(input);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("blub/test.txt");

            changes.delete("testdata/test1.xml");
            archiveListDelete("testdata/test1.xml");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new CpioArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("blub/test.txt");

            changes.deleteDir("blub");
            archiveListDeleteDir("blub");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            changes.deleteDir("bla");

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new CpioArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");

            changes.deleteDir("bla");
            archiveListDeleteDir("bla");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File input = getFile("bla.zip");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);

            temp = File.createTempFile("test", ".zip");
            temp.deleteOnExit();
            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(temp));

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");

        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromTar
    public void testDeleteFromTar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File input = getFile("bla.tar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("tar", is);

            temp = new File(dir, "bla.tar");
            out = factory.createArchiveOutputStream("tar",
                    new FileOutputStream(temp));

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromJar
    public void testDeleteFromJar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
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

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToTar
    public void testDeleteFromAndAddToTar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
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

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        List<String> expected = new ArrayList<String>();
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
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");
            changes.deleteDir("META-INF");
            changes.delete(".classpath");
            changes.delete(".project");

            final File file1 = getFile("test.txt");
            JarArchiveEntry entry = new JarArchiveEntry("testdata/test.txt");
            changes.add(entry, new FileInputStream(file1));

            final File input = getFile("bla.jar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("jar", is);

            temp = new File(dir, "bla.jar");
            out = factory.createArchiveOutputStream("jar",
                    new FileOutputStream(temp));

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        List<String> expected = new ArrayList<String>();
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
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File input = getFile("bla.ar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("ar", is);

            temp = new File(dir, "bla.ar");
            out = factory.createArchiveOutputStream("ar",
                    new FileOutputStream(temp));

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }

        List<String> expected = new ArrayList<String>();
        expected.add("test1.xml");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToAr
    public void testDeleteFromAndAddToAr() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
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

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            performer.perform(ais, out);

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close();
            }
        }
        List<String> expected = new ArrayList<String>();
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
        File input = this.createEmptyArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        InputStream is = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        ChangeSet changes = new ChangeSet();
        try {

            is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");
            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createSingleEntryArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        InputStream is = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        ChangeSet changes = new ChangeSet();
        try {

            is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));
            changes.delete("test1.xml");
            archiveListDelete("test1.xml");

            final File file = getFile("test.txt");
            ArchiveEntry entry = out.createArchiveEntry(file,"bla/test.txt");
            changes.add(entry, new FileInputStream(file));
            archiveList.add("bla/test.txt");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createSingleEntryArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        InputStream is = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        ChangeSet changes = new ChangeSet();
        try {

            is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);

            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));
            final File file = getFile("test.txt");
            ArchiveEntry entry = out.createArchiveEntry(file,"bla/test.txt"); 
            changes.add(entry, new FileInputStream(file));
            archiveList.add("bla/test.txt");

            changes.delete("test1.xml");
            archiveListDelete("test1.xml");

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("testdata/test1.xml");
            changes.add(entry, new FileInputStream(file1), true);

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            ChangeSetResults results = performer.perform(ais, out);
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
        File input = this.createArchive(archivename);

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", "."+archivename);
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream(archivename, is);
            out = factory.createArchiveOutputStream(archivename,
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("testdata/test1.xml");
            changes.add(entry, new FileInputStream(file1), false);

            ChangeSetPerformer performer = new ChangeSetPerformer(changes);
            ChangeSetResults results = performer.perform(ais, out);
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

// org.apache.commons.compress.compressors.Pack200TestCase::testJarUnarchiveAllInMemory
    public void testJarUnarchiveAllInMemory() throws Exception {
        jarUnarchiveAll(false, Pack200Strategy.IN_MEMORY);
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testJarUnarchiveAllFileArgInMemory
    public void testJarUnarchiveAllFileArgInMemory() throws Exception {
        jarUnarchiveAll(true, Pack200Strategy.IN_MEMORY);
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testJarUnarchiveAllTempFile
    public void testJarUnarchiveAllTempFile() throws Exception {
        jarUnarchiveAll(false, Pack200Strategy.TEMP_FILE);
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testJarUnarchiveAllFileTempFile
    public void testJarUnarchiveAllFileTempFile() throws Exception {
        jarUnarchiveAll(true, Pack200Strategy.TEMP_FILE);
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testJarArchiveCreationInMemory
    public void testJarArchiveCreationInMemory() throws Exception {
        jarArchiveCreation(Pack200Strategy.IN_MEMORY);
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testJarArchiveCreationTempFile
    public void testJarArchiveCreationTempFile() throws Exception {
        jarArchiveCreation(Pack200Strategy.TEMP_FILE);
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testGoodSignature
    public void testGoodSignature() throws Exception {
        final InputStream is = new FileInputStream(getFile("bla.pack"));
        try {
            byte[] sig = new byte[4];
            is.read(sig);
            assertTrue(Pack200CompressorInputStream.matches(sig, 4));
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testBadSignature
    public void testBadSignature() throws Exception {
        final InputStream is = new FileInputStream(getFile("bla.jar"));
        try {
            byte[] sig = new byte[4];
            is.read(sig);
            assertFalse(Pack200CompressorInputStream.matches(sig, 4));
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testShortSignature
    public void testShortSignature() throws Exception {
        final InputStream is = new FileInputStream(getFile("bla.pack"));
        try {
            byte[] sig = new byte[2];
            is.read(sig);
            assertFalse(Pack200CompressorInputStream.matches(sig, 2));
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testInputStreamMethods
    public void testInputStreamMethods() throws Exception {
        Map<String, String> m = new HashMap<String, String>();
        m.put("foo", "bar");
        final InputStream is =
            new Pack200CompressorInputStream(new FileInputStream(getFile("bla.jar")),
                                             m);
        try {
            
            
            assertTrue(is.markSupported());
            is.mark(5);
            assertEquals(0x50, is.read());
            byte[] rest = new byte[3];
            assertEquals(3, is.read(rest));
            assertEquals(0x4b, rest[0]);
            assertEquals(3, rest[1]);
            assertEquals(4, rest[2]);
            assertEquals(1, is.skip(1));
            is.reset();
            assertEquals(0x50, is.read());
            assertTrue(is.available() > 0);
        } finally {
            is.close();
        }
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testOutputStreamMethods
    public void testOutputStreamMethods() throws Exception {
        final File output = new File(dir, "bla.pack");
        Map<String, String> m = new HashMap<String, String>();
        m.put("foo", "bar");
        final OutputStream out = new FileOutputStream(output);
        try {
            final OutputStream os = new Pack200CompressorOutputStream(out, m);
            os.write(1);
            os.write(new byte[] { 2, 3 });
            os.close();
        } finally {
            out.close();
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

// org.apache.commons.compress.compressors.pack200.Pack200UtilsTest::testNormalize
    public void testNormalize() throws Throwable {
        final File input = getFile("bla.jar");
        final File[] output = createTempDirAndFile();
        try {
            Pack200Utils.normalize(input, output[1],
                                   new HashMap<String, String>());
            final FileInputStream is = new FileInputStream(output[1]);
            try {
                final ArchiveInputStream in = new ArchiveStreamFactory()
                    .createArchiveInputStream("jar", is);

                ArchiveEntry entry = in.getNextEntry();
                while (entry != null) {
                    File archiveEntry = new File(dir, entry.getName());
                    archiveEntry.getParentFile().mkdirs();
                    if (entry.isDirectory()) {
                        archiveEntry.mkdir();
                        entry = in.getNextEntry();
                        continue;
                    }
                    OutputStream out = new FileOutputStream(archiveEntry);
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
                    File archiveEntry = new File(dir, entry.getName());
                    archiveEntry.getParentFile().mkdirs();
                    if (entry.isDirectory()) {
                        archiveEntry.mkdir();
                        entry = in.getNextEntry();
                        continue;
                    }
                    OutputStream out = new FileOutputStream(archiveEntry);
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
        byte[] b = new byte[12];
        final File input = getFile("bla.tar.sz");
        FileInputStream in = new FileInputStream(input);
        try {
            IOUtils.readFully(in, b);
        } finally {
            in.close();
        }
        assertFalse(FramedSnappyCompressorInputStream.matches(b, 9));
        assertTrue(FramedSnappyCompressorInputStream.matches(b, 10));
        assertTrue(FramedSnappyCompressorInputStream.matches(b, 12));
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testLoremIpsum
    public void testLoremIpsum() throws Exception {
        final FileInputStream isSz = new FileInputStream(getFile("lorem-ipsum.txt.sz"));
        final File outputSz = new File(dir, "lorem-ipsum.1");
        final File outputGz = new File(dir, "lorem-ipsum.2");
        try {
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
            final FileInputStream isGz = new FileInputStream(getFile("lorem-ipsum.txt.gz"));
            try {
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
            } finally {
                isGz.close();
            }
        } finally {
            isSz.close();
        }

        final FileInputStream sz = new FileInputStream(outputSz);
        try {
            FileInputStream gz = new FileInputStream(outputGz);
            try {
                assertArrayEquals(IOUtils.toByteArray(sz),
                                  IOUtils.toByteArray(gz));
            } finally {
                gz.close();
            }
        } finally {
            sz.close();
        }
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testRemainingChunkTypes
    public void testRemainingChunkTypes() throws Exception {
        final FileInputStream isSz = new FileInputStream(getFile("mixed.txt.sz"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            FramedSnappyCompressorInputStream in = new FramedSnappyCompressorInputStream(isSz);
            IOUtils.copy(in, out);
            out.close();
        } finally {
            isSz.close();
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
        final FileInputStream isSz = new FileInputStream(getFile("mixed.txt.sz"));
        try {
            FramedSnappyCompressorInputStream in = new FramedSnappyCompressorInputStream(isSz);
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
        } finally {
            isSz.close();
        }
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testUnskippableChunk
    public void testUnskippableChunk() {
        byte[] input = new byte[] {
            (byte) 0xff, 6, 0, 0, 's', 'N', 'a', 'P', 'p', 'Y',
            2, 2, 0, 0, 1, 1
        };
        try {
            FramedSnappyCompressorInputStream in =
                new FramedSnappyCompressorInputStream(new ByteArrayInputStream(input));
            in.read();
            fail("expected an exception");
        } catch (IOException ex) {
            assertTrue(ex.getMessage().indexOf("unskippable chunk") > -1);
        }
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testChecksumUnmasking
    public void testChecksumUnmasking() {
        testChecksumUnmasking(0xc757l);
        testChecksumUnmasking(0xffffc757l);
    }

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::testChecksumUnmasking
    public void testChecksumUnmasking(long x) {
        assertEquals(Long.toHexString(x),
                     Long.toHexString(FramedSnappyCompressorInputStream
                                      .unmask(mask(x))));
    }
