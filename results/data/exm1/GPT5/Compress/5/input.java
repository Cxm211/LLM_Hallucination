// buggy code
    public int read(byte[] buffer, int start, int length) throws IOException {
        if (closed) {
            throw new IOException("The stream is closed");
        }
        if (inf.finished() || current == null) {
            return -1;
        }

        // avoid int overflow, check null buffer
        if (start <= buffer.length && length >= 0 && start >= 0
            && buffer.length - start >= length) {
            if (current.getMethod() == ZipArchiveOutputStream.STORED) {
                int csize = (int) current.getSize();
                if (readBytesOfEntry >= csize) {
                    return -1;
                }
                if (offsetInBuffer >= lengthOfLastRead) {
                    offsetInBuffer = 0;
                    if ((lengthOfLastRead = in.read(buf)) == -1) {
                        return -1;
                    }
                    count(lengthOfLastRead);
                    bytesReadFromStream += lengthOfLastRead;
                }
                int toRead = length > lengthOfLastRead
                    ? lengthOfLastRead - offsetInBuffer
                    : length;
                if ((csize - readBytesOfEntry) < toRead) {
                    toRead = csize - readBytesOfEntry;
                }
                System.arraycopy(buf, offsetInBuffer, buffer, start, toRead);
                offsetInBuffer += toRead;
                readBytesOfEntry += toRead;
                crc.update(buffer, start, toRead);
                return toRead;
            }
            if (inf.needsInput()) {
                fill();
                if (lengthOfLastRead > 0) {
                    bytesReadFromStream += lengthOfLastRead;
                }
            }
            int read = 0;
            try {
                read = inf.inflate(buffer, start, length);
            } catch (DataFormatException e) {
                throw new ZipException(e.getMessage());
            }
            if (read == 0 && inf.finished()) {
                return -1;
            }
            crc.update(buffer, start, read);
            return read;
        }
        throw new ArrayIndexOutOfBoundsException();
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
            int read;
            while ((read = fis.read(b)) > 0) {
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
            if (archive != null) {
                archive.delete();
            }
            if (fis != null) {
                fis.close();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            int read;
            while ((read = fis.read(b)) > 0) {
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
            if (archive != null) {
                archive.delete();
            }
            if (fis != null) {
                fis.close();
            }
            tmp[1].delete();
            tmp[0].delete();
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

        Map result = new HashMap();
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

        File t = (File)result.get("test1.xml");
        assertTrue("Expected " + t.getAbsolutePath() + " to exist", t.exists());
        assertEquals("length of " + t.getAbsolutePath(), file1Length, t.length());

        t = (File)result.get("test2.xml");
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
            if (archive != null) {
                archive.delete();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            if (archive != null) {
                archive.delete();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            int read;
            while ((read = fis.read(b)) > 0) {
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
            if (archive != null) {
                archive.delete();
            }
            if (fis != null) {
                fis.close();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            int read;
            while ((read = fis.read(b)) > 0) {
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
            if (archive != null) {
                archive.delete();
            }
            if (fis != null) {
                fis.close();
            }
            tmp[1].delete();
            tmp[0].delete();
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
    	byte[] bytes = name.getBytes();
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
            if (archive != null) {
                archive.delete();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            if (archive != null) {
                archive.delete();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            int read;
            while ((read = fis.read(b)) > 0) {
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
            if (archive != null) {
                archive.delete();
            }
            if (fis != null) {
                fis.close();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            int read;
            while ((read = fis.read(b)) > 0) {
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
            if (archive != null) {
                archive.delete();
            }
            if (fis != null) {
                fis.close();
            }
            tmp[1].delete();
            tmp[0].delete();
        }
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

        
        List results = new ArrayList();

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
        File result = (File)results.get(0);
        assertEquals(file1.length(), result.length());
        result = (File)results.get(1);
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

// org.apache.commons.compress.archivers.ZipTestCase::testListAllFilesWithNestedArchive
    public void testListAllFilesWithNestedArchive() throws Exception {
        final File input = getFile("OSX_ArchiveWithNestedArchive.zip");
       
        List results = new ArrayList();

        final InputStream is = new FileInputStream(input);
        ArchiveInputStream in = null;
        try {
            in = new ArchiveStreamFactory().createArchiveInputStream("zip", is);

            ZipArchiveEntry entry = null;
            while((entry = (ZipArchiveEntry)in.getNextEntry()) != null) {
                results.add((entry.getName()));

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
            if (archive != null) {
                archive.delete();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            if (archive != null) {
                archive.delete();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            int read;
            while ((read = fis.read(b)) > 0) {
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
            if (archive != null) {
                archive.delete();
            }
            if (fis != null) {
                fis.close();
            }
            tmp[1].delete();
            tmp[0].delete();
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
            int read;
            while ((read = fis.read(b)) > 0) {
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
            if (archive != null) {
                archive.delete();
            }
            if (fis != null) {
                fis.close();
            }
            tmp[1].delete();
            tmp[0].delete();
        }
    }

// org.apache.commons.compress.archivers.cpio.CpioArchiveInputStreamTest::testCpioUnarchive
    public void testCpioUnarchive() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("./test1.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>./test2.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>\n");
        

        CpioArchiveInputStream in = 
                new CpioArchiveInputStream(new FileInputStream(getFile("bla.cpio")));
        CpioArchiveEntry entry= null;
        
        StringBuffer result = new StringBuffer();
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

// org.apache.commons.compress.archivers.zip.Maven221MultiVolumeTest::testRead7ZipMultiVolumeArchiveForStream
    public void testRead7ZipMultiVolumeArchiveForStream() throws IOException,
	    URISyntaxException {
	
	URL zip = getClass().getResource("/apache-maven-2.2.1.zip.001");
	FileInputStream archive = new FileInputStream(
		new File(new URI(zip.toString())));
	ZipArchiveInputStream zi = null;
	try {
	    zi = new ZipArchiveInputStream(archive,null,false);
	    
	    
	    
	    for (int i = 0; i < ENTRIES.length; i++) {
		assertEquals(ENTRIES[i], zi.getNextEntry().getName());
	    }
	    
	    
	    ArchiveEntry lastEntry = zi.getNextEntry();
	    assertEquals(LAST_ENTRY_NAME, lastEntry.getName());
	    byte [] buffer = new byte [4096];
	    
	    
	    
	    
	    
	    try {
                int read = 0;
		while ((read = zi.read(buffer)) > 0) { }
		fail("shouldn't be able to read from truncated entry");
	    } catch (IOException e) {
                assertEquals("Truncated ZIP file", e.getMessage());
	    }
	    
	    
	    
	    try {
		zi.getNextEntry();
		fail("shouldn't be able to read another entry from truncated"
                     + " file");
	    } catch (IOException e) {
		
	    }
	} finally {
	    if (zi != null) {
		zi.close();
	    }
	}
    }

// org.apache.commons.compress.archivers.zip.Maven221MultiVolumeTest::testRead7ZipMultiVolumeArchiveForFile
    public void testRead7ZipMultiVolumeArchiveForFile()
        throws IOException, URISyntaxException {
	URL zip = getClass().getResource("/apache-maven-2.2.1.zip.001");
        File file = new File(new URI(zip.toString()));
        try {
            new ZipFile(file);
            fail("Expected ZipFile to fail");
        } catch (IOException ex) {
            
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripExplicitUnicodeExtra
    public void testUtf8FileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, true, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripNoEFSExplicitUnicodeExtra
    public void testUtf8FileRoundtripNoEFSExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testCP437FileRoundtripExplicitUnicodeExtra
    public void testCP437FileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CP437, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testASCIIFileRoundtripExplicitUnicodeExtra
    public void testASCIIFileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(US_ASCII, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripImplicitUnicodeExtra
    public void testUtf8FileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, true, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripNoEFSImplicitUnicodeExtra
    public void testUtf8FileRoundtripNoEFSImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testCP437FileRoundtripImplicitUnicodeExtra
    public void testCP437FileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CP437, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testASCIIFileRoundtripImplicitUnicodeExtra
    public void testASCIIFileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(US_ASCII, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testRead7ZipArchive
    public void testRead7ZipArchive() throws IOException, URISyntaxException {
        URL zip = getClass().getResource("/utf8-7zip-test.zip");
        File archive = new File(new URI(zip.toString()));
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
    public void testRead7ZipArchiveForStream() throws IOException,
                                                      URISyntaxException {
        URL zip = getClass().getResource("/utf8-7zip-test.zip");
        FileInputStream archive =
            new FileInputStream(new File(new URI(zip.toString())));
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
    public void testReadWinZipArchive() throws IOException, URISyntaxException {
        URL zip = getClass().getResource("/utf8-winzip-test.zip");
        File archive = new File(new URI(zip.toString()));
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, null, true);
            assertNotNull(zf.getEntry(ASCII_TXT));
            assertNotNull(zf.getEntry(EURO_FOR_DOLLAR_TXT));
            assertNotNull(zf.getEntry(OIL_BARREL_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testReadWinZipArchiveForStream
    public void testReadWinZipArchiveForStream() throws IOException,
                                                      URISyntaxException {
        URL zip = getClass().getResource("/utf8-winzip-test.zip");
        FileInputStream archive =
            new FileInputStream(new File(new URI(zip.toString())));
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
        ZipArchiveInputStream zi = null;
        try {
            createTestFile(file, US_ASCII, false, true);
            FileInputStream archive = new FileInputStream(file);
            zi = new ZipArchiveInputStream(archive, US_ASCII, true);
            assertEquals(OIL_BARREL_TXT, zi.getNextEntry().getName());
            assertEquals(EURO_FOR_DOLLAR_TXT, zi.getNextEntry().getName());
            assertEquals(ASCII_TXT, zi.getNextEntry().getName());
        } finally {
            if (zi != null) {
                zi.close();
            }
            if (file.exists()) {
                file.delete();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testZipArchiveInputStreamReadsUnicodeFields
    public void testZipArchiveInputStreamReadsUnicodeFields()
        throws IOException {
        File file = File.createTempFile("unicode-test", ".zip");
        ZipFile zf = null;
        try {
            createTestFile(file, US_ASCII, false, true);
            zf = new ZipFile(file, US_ASCII, true);
            assertNotNull(zf.getEntry(ASCII_TXT));
            assertNotNull(zf.getEntry(EURO_FOR_DOLLAR_TXT));
            assertNotNull(zf.getEntry(OIL_BARREL_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
            if (file.exists()) {
                file.delete();
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
            Change c = (Change)changes.getChanges().iterator().next();
            assertEquals(in2, c.getInput());
        } finally {
            if (in != null)
                in.close();
            if (in2 != null)
                in2.close();
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
            Change c = (Change)changes.getChanges().iterator().next();
            assertEquals(in, c.getInput());
        } finally {
            if (in != null)
                in.close();
            if (in2 != null)
                in2.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            assertEquals("bla/test.txt",(String)results.getAddedFromChangeSet().iterator().next());
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            assertTrue(results.getAddedFromChangeSet().size() == 0);
            assertTrue(results.getDeleted().size() == 0);
            is.close();

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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

// org.apache.commons.compress.compressors.GZipTestCase::testGzipCreation
	public void testGzipCreation()  throws Exception {
		final File input = getFile("test1.xml");
		final File output = new File(dir, "test1.xml.gz");
		final OutputStream out = new FileOutputStream(output);
		final CompressorOutputStream cos = new CompressorStreamFactory().createCompressorOutputStream("gz", out);
		IOUtils.copy(new FileInputStream(input), cos);
		cos.close();
	}

// org.apache.commons.compress.compressors.GZipTestCase::testGzipUnarchive
	public void testGzipUnarchive() throws Exception {
		final File input = getFile("bla.tgz");
		final File output = new File(dir, "bla.tar");
        final InputStream is = new FileInputStream(input);
        final CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("gz", is);
        FileOutputStream out = new FileOutputStream(output);
        IOUtils.copy(in, out);
		in.close();
		is.close();
		out.close();
    }
