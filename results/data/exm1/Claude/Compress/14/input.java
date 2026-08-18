// buggy code
    public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long    result = 0;
        int     end = offset + length;
        int     start = offset;

        if (length < 2){
            throw new IllegalArgumentException("Length "+length+" must be at least 2");
        }

        boolean allNUL = true;
        for (int i = start; i < end; i++){
            if (buffer[i] != 0){
                allNUL = false;
                break;
            }
        }
        if (allNUL) {
            return 0L;
        }

        // Skip leading spaces
        while (start < end){
            if (buffer[start] == ' '){
                start++;
            } else {
                break;
            }
        }

        // Must have trailing NUL or space
        byte trailer;
        trailer = buffer[end-1];
        if (trailer == 0 || trailer == ' '){
            end--;
        } else {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, end-1, trailer));
        }
        // May have additional NUL or space
        trailer = buffer[end-1];
        if (trailer == 0 || trailer == ' '){
            end--;
        }

        for ( ;start < end; start++) {
            final byte currentByte = buffer[start];
            // CheckStyle:MagicNumber OFF
            if (currentByte < '0' || currentByte > '7'){
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (currentByte - '0'); // convert from ASCII
            // CheckStyle:MagicNumber ON
        }

        return result;
    }

// relevant test
// org.apache.commons.compress.ChainingTestCase::testTarGzip
    public void testTarGzip() throws Exception {
        File file = new File("src/test/resources/bla.tgz");
        final TarArchiveInputStream is = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
        final TarArchiveEntry entry = (TarArchiveEntry)is.getNextEntry();
        assertNotNull(entry);
        assertEquals("test1.xml", entry.getName());
        is.close();
    }

// org.apache.commons.compress.ChainingTestCase::testTarBzip2
    public void testTarBzip2() throws Exception {
        File file = new File("src/test/resources/bla.tar.bz2");
        final TarArchiveInputStream is = new TarArchiveInputStream(new BZip2CompressorInputStream(new FileInputStream(file)));
        final TarArchiveEntry entry = (TarArchiveEntry)is.getNextEntry();
        assertNotNull(entry);
        assertEquals("test1.xml", entry.getName());
        is.close();
    }

// org.apache.commons.compress.DetectArchiverTestCase::testDetectionNotArchive
    public void testDetectionNotArchive() throws FileNotFoundException {
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
        byte[] bytes = name.getBytes("UTF-8");
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
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("tar", is);
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

// org.apache.commons.compress.archivers.tar.SparseFilesTest::testOldGNU
    public void testOldGNU() throws Throwable {
        URL tar = getClass().getResource("/oldgnu_sparse.tar");
        File file = new File(new URI(tar.toString()));
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
            t = tin.getNextTarEntry();
            assertNotNull(t);
            assertEquals("foo.txt", t.getName());
            t = tin.getNextTarEntry();
            assertNotNull(t);
            assertEquals("bar.txt", t.getName());
            t = tin.getNextTarEntry();
            assertNotNull(t);
            assertEquals("baz.txt", t.getName());
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

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readSimplePaxHeader
    public void readSimplePaxHeader() throws Exception {
        Map<String, String> headers = new TarArchiveInputStream(null)
            .parsePaxHeaders(new StringReader("30 atime=1321711775.972059463\n"));
        assertEquals(1, headers.size());
        assertEquals("1321711775.972059463", headers.get("atime"));
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::readPaxHeaderWithEmbeddedNewline
    public void readPaxHeaderWithEmbeddedNewline() throws Exception {
        Map<String, String> headers = new TarArchiveInputStream(null)
            .parsePaxHeaders(new StringReader("28 comment=line1\nline2\nand3\n"));
        assertEquals(1, headers.size());
        assertEquals("line1\nline2\nand3", headers.get("comment"));
    }

// org.apache.commons.compress.archivers.tar.TarArchiveInputStreamTest::workaroundForBrokenTimeHeader
    public void workaroundForBrokenTimeHeader() throws Exception {
        URL tar = getClass().getResource("/simple-aix-native-tar.tar");
        TarArchiveInputStream in = null;
        try {
            in = new TarArchiveInputStream(new FileInputStream(new File(new URI(tar.toString()))));
            TarArchiveEntry tae = in.getNextTarEntry();
            tae = in.getNextTarEntry();
            assertEquals("sample/link-to-txt-file.lnk", tae.getName());
            assertEquals(new Date(0), tae.getLastModifiedDate());
            assertTrue(tae.isSymbolicLink());
        } finally {
            if (in != null) {
                in.close();
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
        TarArchiveEntry sEntry = new TarArchiveEntry(file1);
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

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testBigFileStarMode
    public void testBigFileStarMode() throws Exception {
        TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(0100000000000L);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigFileMode(TarArchiveOutputStream.BIGFILE_STAR);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        byte[] data = bos.toByteArray();
        assertEquals(0x80,
                     ((int) data[TarConstants.NAMELEN
                                 + TarConstants.MODELEN
                                 + TarConstants.UIDLEN
                                 + TarConstants.GIDLEN]
                      ) & 0x80);
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(0100000000000L, e.getSize());
    }

// org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testBigFilePosixMode
    public void testBigFilePosixMode() throws Exception {
        TarArchiveEntry t = new TarArchiveEntry("foo");
        t.setSize(0100000000000L);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigFileMode(TarArchiveOutputStream.BIGFILE_POSIX);
        tos.putArchiveEntry(t);
        
        tos.write(new byte[10 * 1024]);
        byte[] data = bos.toByteArray();
        assertEquals("00000000000 ",
                     new String(data,
                                1024 + TarConstants.NAMELEN
                                + TarConstants.MODELEN
                                + TarConstants.UIDLEN
                                + TarConstants.GIDLEN, 12,
                                "UTF-8"));
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(0100000000000L, e.getSize());
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
                                "UTF-8"));
        assertEquals("6 a=b\n", new String(data, 512, 6, "UTF-8"));
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
                                "UTF-8"));
        assertEquals("99 a=0123456789012345678901234567890123456789"
              + "01234567890123456789012345678901234567890123456789"
              + "012\n", new String(data, 512, 99, "UTF-8"));
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
                                "UTF-8"));
        assertEquals("101 a=0123456789012345678901234567890123456789"
              + "01234567890123456789012345678901234567890123456789"
              + "0123\n", new String(data, 512, 101, "UTF-8"));
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
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        tos.putArchiveEntry(t);
        tos.write(new byte[10 * 1024]);
        tos.closeArchiveEntry();
        byte[] data = bos.toByteArray();
        assertEquals("160 path=" + n + "\n",
                     new String(data, 512, 160, "UTF-8"));
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testName
    public void testName(){
        byte [] buff = new byte[20];
        String sb1 = "abcdefghijklmnopqrstuvwxyz";
        int off = TarUtils.formatNameBytes(sb1, buff, 1, buff.length-1);
        assertEquals(off, 20);
        String sb2 = TarUtils.parseName(buff, 1, 10);
        assertEquals(sb2,sb1.substring(0,10));
        sb2 = TarUtils.parseName(buff, 1, 19);
        assertEquals(sb2,sb1.substring(0,19));
        buff = new byte[30];
        off = TarUtils.formatNameBytes(sb1, buff, 1, buff.length-1);
        assertEquals(off, 30);
        sb2 = TarUtils.parseName(buff, 1, buff.length-1);
        assertEquals(sb1, sb2);
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testParseOctal
    public void testParseOctal() throws Exception{
        long value; 
        byte [] buffer;
        final long MAX_OCTAL  = 077777777777L; 
        final String maxOctal = "77777777777 "; 
        buffer = maxOctal.getBytes("UTF-8");
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(MAX_OCTAL, value);
        buffer[buffer.length-1]=0;
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(MAX_OCTAL, value);
        buffer=new byte[]{0,0};
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(0, value);        
        buffer=new byte[]{0,' '};
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(0, value);        
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testParseOctalInvalid
    public void testParseOctalInvalid() throws Exception{
        byte [] buffer;
        buffer=new byte[0]; 
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - should be at least 2 bytes long");
        } catch (IllegalArgumentException expected) {
        }
        buffer=new byte[]{0}; 
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - should be at least 2 bytes long");
        } catch (IllegalArgumentException expected) {
        }
        buffer=new byte[]{' ',0,0,0}; 
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - not all NULs");
        } catch (IllegalArgumentException expected) {
        }
        buffer = "abcdef ".getBytes("UTF-8"); 
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        buffer = "77777777777".getBytes("UTF-8"); 
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - no trailer");
        } catch (IllegalArgumentException expected) {
        }
        buffer = " 0 07 ".getBytes("UTF-8"); 
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - embedded space");
        } catch (IllegalArgumentException expected) {
        }
        buffer = " 0\00007 ".getBytes("UTF-8"); 
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - embedded NUL");
        } catch (IllegalArgumentException expected) {
        }
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testRoundTripOctal
    public void testRoundTripOctal() {
        checkRoundTripOctal(0);
        checkRoundTripOctal(1);

        checkRoundTripOctal(TarConstants.MAXSIZE);

        checkRoundTripOctal(0, TarConstants.UIDLEN);
        checkRoundTripOctal(1, TarConstants.UIDLEN);
        checkRoundTripOctal(TarConstants.MAXID, 8);
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testRoundTripOctalOrBinary
    public void testRoundTripOctalOrBinary() {
        checkRoundTripOctalOrBinary(0, 8);
        checkRoundTripOctalOrBinary(1, 8);
        checkRoundTripOctalOrBinary(Long.MAX_VALUE, 8); 
        checkRoundTripOctalOrBinary(TarConstants.MAXSIZE, 8); 
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testTrailers
    public void testTrailers() {
        byte [] buffer = new byte[12];
        TarUtils.formatLongOctalBytes(123, buffer, 0, buffer.length);
        assertEquals(' ', buffer[buffer.length-1]);
        assertEquals('3', buffer[buffer.length-2]); 
        TarUtils.formatOctalBytes(123, buffer, 0, buffer.length);
        assertEquals(0  , buffer[buffer.length-1]);
        assertEquals(' ', buffer[buffer.length-2]);
        assertEquals('3', buffer[buffer.length-3]); 
        TarUtils.formatCheckSumOctalBytes(123, buffer, 0, buffer.length);
        assertEquals(' ', buffer[buffer.length-1]);
        assertEquals(0  , buffer[buffer.length-2]);
        assertEquals('3', buffer[buffer.length-3]); 
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testNegative
    public void testNegative() throws Exception {
        byte [] buffer = new byte[22];
        TarUtils.formatUnsignedOctalString(-1, buffer, 0, buffer.length);
        assertEquals("1777777777777777777777", new String(buffer, "UTF-8"));
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testOverflow
    public void testOverflow() throws Exception {
        byte [] buffer = new byte[8-1]; 
        TarUtils.formatUnsignedOctalString(07777777L, buffer, 0, buffer.length);
        assertEquals("7777777", new String(buffer, "UTF-8"));        
        try {
            TarUtils.formatUnsignedOctalString(017777777L, buffer, 0, buffer.length);
            fail("Should have cause IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

// org.apache.commons.compress.archivers.tar.TarUtilsTest::testRoundTripNames
    public void testRoundTripNames(){
        checkName("");
        checkName("The quick brown fox\n");
        checkName("\177");
        
        
        checkName("0302-0601-3\u00b1\u00b1\u00b1F06\u00b1W220\u00b1ZB\u00b1LALALA\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1\u00b1CAN\u00b1\u00b1DC\u00b1\u00b1\u00b104\u00b1060302\u00b1MOE.model");
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
            Change c = changes.getChanges().iterator().next();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
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
            assertTrue(results.getAddedFromChangeSet().isEmpty());
            assertTrue(results.getDeleted().isEmpty());
            is.close();

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        this.checkArchiveContent(result, archiveList);
    }
