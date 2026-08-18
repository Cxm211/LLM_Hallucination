// buggy code
    public void reparseCentralDirectoryData(boolean hasUncompressedSize,
                                            boolean hasCompressedSize,
                                            boolean hasRelativeHeaderOffset,
                                            boolean hasDiskStart)
        throws ZipException {
        if (rawCentralDirectoryData != null) {
            int expectedLength = (hasUncompressedSize ? DWORD : 0)
                + (hasCompressedSize ? DWORD : 0)
                + (hasRelativeHeaderOffset ? DWORD : 0)
                + (hasDiskStart ? WORD : 0);
            if (rawCentralDirectoryData.length != expectedLength) {
                throw new ZipException("central directory zip64 extended"
                                       + " information extra field's length"
                                       + " doesn't match central directory"
                                       + " data.  Expected length "
                                       + expectedLength + " but is "
                                       + rawCentralDirectoryData.length);
            }
            int offset = 0;
            if (hasUncompressedSize) {
                size = new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += DWORD;
            }
            if (hasCompressedSize) {
                compressedSize = new ZipEightByteInteger(rawCentralDirectoryData,
                                                         offset);
                offset += DWORD;
            }
            if (hasRelativeHeaderOffset) {
                relativeHeaderOffset =
                    new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += DWORD;
            }
            if (hasDiskStart) {
                diskStart = new ZipLong(rawCentralDirectoryData, offset);
                offset += WORD;
            }
        }
    }

// relevant test
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
        ZipFile bla = new ZipFile(getFile("bla.zip"));
        assertTrue(bla.canReadEntryData(bla.getEntry("test1.xml")));
        bla.close();

        ZipFile moby = new ZipFile(getFile("moby.zip"));
        assertFalse(moby.canReadEntryData(moby.getEntry("README")));
        moby.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testSkipEntryWithUnsupportedCompressionMethod
    public void testSkipEntryWithUnsupportedCompressionMethod()
            throws IOException {
        ZipArchiveInputStream zip =
            new ZipArchiveInputStream(new FileInputStream(getFile("moby.zip")));
        try {
            ZipArchiveEntry entry = zip.getNextZipEntry();
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

// org.apache.commons.compress.archivers.jar.JarArchiveOutputStreamTest::testJarMarker
    public void testJarMarker() throws IOException {
        File testArchive = File.createTempFile("jar-aostest", ".jar");
        testArchive.deleteOnExit();
        JarArchiveOutputStream out = null;
        ZipFile zf = null;
        try {

            out = new JarArchiveOutputStream(new FileOutputStream(testArchive));
            out.putArchiveEntry(new ZipArchiveEntry("foo/"));
            out.closeArchiveEntry();
            out.putArchiveEntry(new ZipArchiveEntry("bar/"));
            out.closeArchiveEntry();
            out.finish();
            out.close();
            out = null;

            zf = new ZipFile(testArchive);
            ZipArchiveEntry ze = zf.getEntry("foo/");
            assertNotNull(ze);
            ZipExtraField[] fes = ze.getExtraFields();
            assertEquals(1, fes.length);
            assertTrue(fes[0] instanceof JarMarker);

            ze = zf.getEntry("bar/");
            assertNotNull(ze);
            fes = ze.getExtraFields();
            assertEquals(0, fes.length);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {  }
            }
            ZipFile.closeQuietly(zf);
            AbstractTestCase.tryHardToDelete(testArchive);
        }
    }

// org.apache.commons.compress.archivers.zip.EncryptedArchiveTest::testReadPasswordEncryptedEntryViaZipFile
    public void testReadPasswordEncryptedEntryViaZipFile()
        throws IOException {
        File file = getFile("password-encrypted.zip");
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            ZipArchiveEntry zae = zf.getEntry("LICENSE.txt");
            assertTrue(zae.getGeneralPurposeBit().usesEncryption());
            assertFalse(zae.getGeneralPurposeBit().usesStrongEncryption());
            assertFalse(zf.canReadEntryData(zae));
            try {
                zf.getInputStream(zae);
                fail("expected an exception");
            } catch (UnsupportedZipFeatureException ex) {
                assertSame(UnsupportedZipFeatureException.Feature.ENCRYPTION,
                           ex.getFeature());
            }
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

// org.apache.commons.compress.archivers.zip.EncryptedArchiveTest::testReadPasswordEncryptedEntryViaStream
    public void testReadPasswordEncryptedEntryViaStream()
        throws IOException {
        File file = getFile("password-encrypted.zip");
        ZipArchiveInputStream zin = null;
        try {
            zin = new ZipArchiveInputStream(new FileInputStream(file));
            ZipArchiveEntry zae = zin.getNextZipEntry();
            assertEquals("LICENSE.txt", zae.getName());
            assertTrue(zae.getGeneralPurposeBit().usesEncryption());
            assertFalse(zae.getGeneralPurposeBit().usesStrongEncryption());
            assertFalse(zin.canReadEntryData(zae));
            try {
                byte[] buf = new byte[1024];
                zin.read(buf, 0, buf.length);
                fail("expected an exception");
            } catch (UnsupportedZipFeatureException ex) {
                assertSame(UnsupportedZipFeatureException.Feature.ENCRYPTION,
                           ex.getFeature());
            }
        } finally {
            if (zin != null) {
                zin.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.ExtraFieldUtilsTest::testParse
    public void testParse() throws Exception {
        ZipExtraField[] ze = ExtraFieldUtils.parse(data);
        assertEquals("number of fields", 2, ze.length);
        assertTrue("type field 1", ze[0] instanceof AsiExtraField);
        assertEquals("mode field 1", 040755,
                     ((AsiExtraField) ze[0]).getMode());
        assertTrue("type field 2", ze[1] instanceof UnrecognizedExtraField);
        assertEquals("data length field 2", 1,
                     ze[1].getLocalFileDataLength().getValue());

        byte[] data2 = new byte[data.length-1];
        System.arraycopy(data, 0, data2, 0, data2.length);
        try {
            ExtraFieldUtils.parse(data2);
            fail("data should be invalid");
        } catch (Exception e) {
            assertEquals("message",
                         "bad extra field starting at "+(4 + aLocal.length)
                         + ".  Block length of 1 bytes exceeds remaining data of 0 bytes.",
                         e.getMessage());
        }
    }

// org.apache.commons.compress.archivers.zip.ExtraFieldUtilsTest::testParseWithRead
    public void testParseWithRead() throws Exception {
        ZipExtraField[] ze =
            ExtraFieldUtils.parse(data, true,
                                  ExtraFieldUtils.UnparseableExtraField.READ);
        assertEquals("number of fields", 2, ze.length);
        assertTrue("type field 1", ze[0] instanceof AsiExtraField);
        assertEquals("mode field 1", 040755,
                     ((AsiExtraField) ze[0]).getMode());
        assertTrue("type field 2", ze[1] instanceof UnrecognizedExtraField);
        assertEquals("data length field 2", 1,
                     ze[1].getLocalFileDataLength().getValue());

        byte[] data2 = new byte[data.length-1];
        System.arraycopy(data, 0, data2, 0, data2.length);
        ze = ExtraFieldUtils.parse(data2, true,
                                   ExtraFieldUtils.UnparseableExtraField.READ);
        assertEquals("number of fields", 2, ze.length);
        assertTrue("type field 1", ze[0] instanceof AsiExtraField);
        assertEquals("mode field 1", 040755,
                     ((AsiExtraField) ze[0]).getMode());
        assertTrue("type field 2", ze[1] instanceof UnparseableExtraFieldData);
        assertEquals("data length field 2", 4,
                     ze[1].getLocalFileDataLength().getValue());
        for (int i = 0; i < 4; i++) {
            assertEquals("byte number " + i,
                         data2[data.length - 5 + i],
                         ze[1].getLocalFileDataData()[i]);
        }
    }

// org.apache.commons.compress.archivers.zip.ExtraFieldUtilsTest::testParseWithSkip
    public void testParseWithSkip() throws Exception {
        ZipExtraField[] ze =
            ExtraFieldUtils.parse(data, true,
                                  ExtraFieldUtils.UnparseableExtraField.SKIP);
        assertEquals("number of fields", 2, ze.length);
        assertTrue("type field 1", ze[0] instanceof AsiExtraField);
        assertEquals("mode field 1", 040755,
                     ((AsiExtraField) ze[0]).getMode());
        assertTrue("type field 2", ze[1] instanceof UnrecognizedExtraField);
        assertEquals("data length field 2", 1,
                     ze[1].getLocalFileDataLength().getValue());

        byte[] data2 = new byte[data.length-1];
        System.arraycopy(data, 0, data2, 0, data2.length);
        ze = ExtraFieldUtils.parse(data2, true,
                                   ExtraFieldUtils.UnparseableExtraField.SKIP);
        assertEquals("number of fields", 1, ze.length);
        assertTrue("type field 1", ze[0] instanceof AsiExtraField);
        assertEquals("mode field 1", 040755,
                     ((AsiExtraField) ze[0]).getMode());
    }

// org.apache.commons.compress.archivers.zip.ExtraFieldUtilsTest::testMerge
    public void testMerge() {
        byte[] local =
            ExtraFieldUtils.mergeLocalFileDataData(new ZipExtraField[] {a, dummy});
        assertEquals("local length", data.length, local.length);
        for (int i=0; i<local.length; i++) {
            assertEquals("local byte "+i, data[i], local[i]);
        }

        byte[] dummyCentral = dummy.getCentralDirectoryData();
        byte[] data2 = new byte[4 + aLocal.length + 4 + dummyCentral.length];
        System.arraycopy(data, 0, data2, 0, 4 + aLocal.length + 2);
        System.arraycopy(dummy.getCentralDirectoryLength().getBytes(), 0,
                         data2, 4+aLocal.length+2, 2);
        System.arraycopy(dummyCentral, 0, data2,
                         4+aLocal.length+4, dummyCentral.length);

        byte[] central =
            ExtraFieldUtils.mergeCentralDirectoryData(new ZipExtraField[] {a, dummy});
        assertEquals("central length", data2.length, central.length);
        for (int i=0; i<central.length; i++) {
            assertEquals("central byte "+i, data2[i], central[i]);
        }

    }

// org.apache.commons.compress.archivers.zip.ExtraFieldUtilsTest::testMergeWithUnparseableData
    public void testMergeWithUnparseableData() throws Exception {
        ZipExtraField d = new UnparseableExtraFieldData();
        byte[] b = UNRECOGNIZED_HEADER.getBytes();
        d.parseFromLocalFileData(new byte[] {b[0], b[1], 1, 0}, 0, 4);
        byte[] local =
            ExtraFieldUtils.mergeLocalFileDataData(new ZipExtraField[] {a, d});
        assertEquals("local length", data.length - 1, local.length);
        for (int i = 0; i < local.length; i++) {
            assertEquals("local byte " + i, data[i], local[i]);
        }

        byte[] dCentral = d.getCentralDirectoryData();
        byte[] data2 = new byte[4 + aLocal.length + dCentral.length];
        System.arraycopy(data, 0, data2, 0, 4 + aLocal.length + 2);
        System.arraycopy(dCentral, 0, data2,
                         4 + aLocal.length, dCentral.length);

        byte[] central =
            ExtraFieldUtils.mergeCentralDirectoryData(new ZipExtraField[] {a, d});
        assertEquals("central length", data2.length, central.length);
        for (int i = 0; i < central.length; i++) {
            assertEquals("central byte " + i, data2[i], central[i]);
        }

    }

// org.apache.commons.compress.archivers.zip.Maven221MultiVolumeTest::testRead7ZipMultiVolumeArchiveForStream
    public void testRead7ZipMultiVolumeArchiveForStream() throws IOException {

        FileInputStream archive =
            new FileInputStream(getFile("apache-maven-2.2.1.zip.001"));
        ZipArchiveInputStream zi = null;
        try {
            zi = new ZipArchiveInputStream(archive,null,false);

            
            
            for (String element : ENTRIES) {
                assertEquals(element, zi.getNextEntry().getName());
            }

            
            ArchiveEntry lastEntry = zi.getNextEntry();
            assertEquals(LAST_ENTRY_NAME, lastEntry.getName());
            byte [] buffer = new byte [4096];

            
            
            
            
            try {
                while (zi.read(buffer) > 0) { }
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
    public void testRead7ZipMultiVolumeArchiveForFile() throws IOException {
        File file = getFile("apache-maven-2.2.1.zip.001");
        try {
            new ZipFile(file);
            fail("Expected ZipFile to fail");
        } catch (IOException ex) {
            
        }
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

// org.apache.commons.compress.archivers.zip.X7875_NewUnixTest::testSampleFile
    public void testSampleFile() throws Exception {
        File archive = getFile("COMPRESS-211_uid_gid_zip_test.zip");
        ZipFile zf = null;

        try {
            zf = new ZipFile(archive);
            Enumeration<ZipArchiveEntry> en = zf.getEntries();

            
            
            while (en.hasMoreElements()) {

                ZipArchiveEntry zae = en.nextElement();
                String name = zae.getName();
                X7875_NewUnix xf = (X7875_NewUnix) zae.getExtraField(X7875);

                
                long expected = 1000;
                if (name.contains("uid555_gid555")) {
                    expected = 555;
                } else if (name.contains("uid5555_gid5555")) {
                    expected = 5555;
                } else if (name.contains("uid55555_gid55555")) {
                    expected = 55555;
                } else if (name.contains("uid555555_gid555555")) {
                    expected = 555555;
                } else if (name.contains("min_unix")) {
                    expected = 0;
                } else if (name.contains("max_unix")) {
                    
                    
                    expected = 0x100000000L - 2;
                }
                assertEquals(expected, xf.getUID());
                assertEquals(expected, xf.getGID());
            }
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.X7875_NewUnixTest::testGetHeaderId
    public void testGetHeaderId() {
        assertEquals(X7875, xf.getHeaderId());
    }

// org.apache.commons.compress.archivers.zip.X7875_NewUnixTest::testMisc
    public void testMisc() throws Exception {
        assertFalse(xf.equals(new Object()));
        assertTrue(xf.toString().startsWith("0x7875 Zip Extra Field"));
        Object o = xf.clone();
        assertEquals(o.hashCode(), xf.hashCode());
        assertTrue(xf.equals(o));
        xf.setUID(12345);
        assertFalse(xf.equals(o));
    }

// org.apache.commons.compress.archivers.zip.X7875_NewUnixTest::testTrimLeadingZeroesForceMinLength4
    public void testTrimLeadingZeroesForceMinLength4() {
        final byte[] NULL = null;
        final byte[] EMPTY = new byte[0];
        final byte[] ONE_ZERO = {0};
        final byte[] TWO_ZEROES = {0, 0};
        final byte[] FOUR_ZEROES = {0, 0, 0, 0};
        final byte[] SEQUENCE = {1, 2, 3};
        final byte[] SEQUENCE_LEADING_ZERO = {0, 1, 2, 3};
        final byte[] SEQUENCE_LEADING_ZEROES = {0, 0, 0, 0, 0, 0, 0, 1, 2, 3};
        final byte[] TRAILING_ZERO = {1, 2, 3, 0};
        final byte[] PADDING_ZERO = {0, 1, 2, 3, 0};
        final byte[] SEQUENCE6 = {1, 2, 3, 4, 5, 6};
        final byte[] SEQUENCE6_LEADING_ZERO = {0, 1, 2, 3, 4, 5, 6};

        assertTrue(NULL == trimTest(NULL));
        assertTrue(Arrays.equals(ONE_ZERO, trimTest(EMPTY)));
        assertTrue(Arrays.equals(ONE_ZERO, trimTest(ONE_ZERO)));
        assertTrue(Arrays.equals(ONE_ZERO, trimTest(TWO_ZEROES)));
        assertTrue(Arrays.equals(ONE_ZERO, trimTest(FOUR_ZEROES)));
        assertTrue(Arrays.equals(SEQUENCE, trimTest(SEQUENCE)));
        assertTrue(Arrays.equals(SEQUENCE, trimTest(SEQUENCE_LEADING_ZERO)));
        assertTrue(Arrays.equals(SEQUENCE, trimTest(SEQUENCE_LEADING_ZEROES)));
        assertTrue(Arrays.equals(TRAILING_ZERO, trimTest(TRAILING_ZERO)));
        assertTrue(Arrays.equals(TRAILING_ZERO, trimTest(PADDING_ZERO)));
        assertTrue(Arrays.equals(SEQUENCE6, trimTest(SEQUENCE6)));
        assertTrue(Arrays.equals(SEQUENCE6, trimTest(SEQUENCE6_LEADING_ZERO)));
    }

// org.apache.commons.compress.archivers.zip.X7875_NewUnixTest::testParseReparse
    public void testParseReparse() throws ZipException {

        
        final byte[] ZERO_LEN = {1, 0, 0};

        
        final byte[] ZERO_UID_GID = {1, 1, 0, 1, 0};

        
        final byte[] ONE_UID_GID = {1, 1, 1, 1, 1};

        
        final byte[] ONE_THOUSAND_UID_GID = {1, 2, -24, 3, 2, -24, 3};

        
        
        final byte[] UNIX_MAX_UID_GID = {1, 4, -2, -1, -1, -1, 4, -2, -1, -1, -1};

        
        
        final byte[] LENGTH_5 = {1, 5, 0, 0, 0, 0, 1, 5, 1, 0, 0, 0, 1};

        
        
        final byte[] LENGTH_8 = {1, 8, -2, -1, -1, -1, -1, -1, -1, 127, 8, -1, -1, -1, -1, -1, -1, -1, 127};

        final long TWO_TO_32 = 0x100000000L;
        final long MAX = TWO_TO_32 - 2;

        parseReparse(0, 0, ZERO_LEN, 0, 0);
        parseReparse(0, 0, ZERO_UID_GID, 0, 0);
        parseReparse(1, 1, ONE_UID_GID, 1, 1);
        parseReparse(1000, 1000, ONE_THOUSAND_UID_GID, 1000, 1000);
        parseReparse(MAX, MAX, UNIX_MAX_UID_GID, MAX, MAX);
        parseReparse(-2, -2, UNIX_MAX_UID_GID, MAX, MAX);
        parseReparse(TWO_TO_32, TWO_TO_32 + 1, LENGTH_5, TWO_TO_32, TWO_TO_32 + 1);
        parseReparse(Long.MAX_VALUE - 1, Long.MAX_VALUE, LENGTH_8, Long.MAX_VALUE - 1, Long.MAX_VALUE);

        
        final byte[] SPURIOUS_ZEROES_1 = {1, 4, -1, 0, 0, 0, 4, -128, 0, 0, 0};
        final byte[] EXPECTED_1 = {1, 1, -1, 1, -128};
        xf.parseFromLocalFileData(SPURIOUS_ZEROES_1, 0, SPURIOUS_ZEROES_1.length);

        assertEquals(255, xf.getUID());
        assertEquals(128, xf.getGID());
        assertTrue(Arrays.equals(EXPECTED_1, xf.getLocalFileDataData()));

        final byte[] SPURIOUS_ZEROES_2 = {1, 4, -1, -1, 0, 0, 4, 1, 2, 0, 0};
        final byte[] EXPECTED_2 = {1, 2, -1, -1, 2, 1, 2};
        xf.parseFromLocalFileData(SPURIOUS_ZEROES_2, 0, SPURIOUS_ZEROES_2.length);

        assertEquals(65535, xf.getUID());
        assertEquals(513, xf.getGID());
        assertTrue(Arrays.equals(EXPECTED_2, xf.getLocalFileDataData()));
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testWriteCDOnlySizes
    public void testWriteCDOnlySizes() {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField(SIZE, CSIZE);
        assertEquals(new ZipShort(16), f.getCentralDirectoryLength());
        byte[] b = f.getCentralDirectoryData();
        assertEquals(16, b.length);
        checkSizes(b);
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testWriteCDSizeAndOffset
    public void testWriteCDSizeAndOffset() {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField(SIZE, CSIZE, OFF, null);
        assertEquals(new ZipShort(24), f.getCentralDirectoryLength());
        byte[] b = f.getCentralDirectoryData();
        assertEquals(24, b.length);
        checkSizes(b);
        checkOffset(b, 16);
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testWriteCDSizeOffsetAndDisk
    public void testWriteCDSizeOffsetAndDisk() {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField(SIZE, CSIZE, OFF, DISK);
        assertEquals(new ZipShort(28), f.getCentralDirectoryLength());
        byte[] b = f.getCentralDirectoryData();
        assertEquals(28, b.length);
        checkSizes(b);
        checkOffset(b, 16);
        checkDisk(b, 24);
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testWriteCDSizeAndDisk
    public void testWriteCDSizeAndDisk() {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField(SIZE, CSIZE, null, DISK);
        assertEquals(new ZipShort(20), f.getCentralDirectoryLength());
        byte[] b = f.getCentralDirectoryData();
        assertEquals(20, b.length);
        checkSizes(b);
        checkDisk(b, 16);
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testReadLFHSizesOnly
    public void testReadLFHSizesOnly() throws ZipException {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField();
        byte[] b = new byte[16];
        System.arraycopy(SIZE.getBytes(), 0, b, 0, 8);
        System.arraycopy(CSIZE.getBytes(), 0, b, 8, 8);
        f.parseFromLocalFileData(b, 0, b.length);
        assertEquals(SIZE, f.getSize());
        assertEquals(CSIZE, f.getCompressedSize());
        assertNull(f.getRelativeHeaderOffset());
        assertNull(f.getDiskStartNumber());
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testReadLFHSizesAndOffset
    public void testReadLFHSizesAndOffset() throws ZipException {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField();
        byte[] b = new byte[24];
        System.arraycopy(SIZE.getBytes(), 0, b, 0, 8);
        System.arraycopy(CSIZE.getBytes(), 0, b, 8, 8);
        System.arraycopy(OFF.getBytes(), 0, b, 16, 8);
        f.parseFromLocalFileData(b, 0, b.length);
        assertEquals(SIZE, f.getSize());
        assertEquals(CSIZE, f.getCompressedSize());
        assertEquals(OFF, f.getRelativeHeaderOffset());
        assertNull(f.getDiskStartNumber());
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testReadLFHSizesOffsetAndDisk
    public void testReadLFHSizesOffsetAndDisk() throws ZipException {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField();
        byte[] b = new byte[28];
        System.arraycopy(SIZE.getBytes(), 0, b, 0, 8);
        System.arraycopy(CSIZE.getBytes(), 0, b, 8, 8);
        System.arraycopy(OFF.getBytes(), 0, b, 16, 8);
        System.arraycopy(DISK.getBytes(), 0, b, 24, 4);
        f.parseFromLocalFileData(b, 0, b.length);
        assertEquals(SIZE, f.getSize());
        assertEquals(CSIZE, f.getCompressedSize());
        assertEquals(OFF, f.getRelativeHeaderOffset());
        assertEquals(DISK, f.getDiskStartNumber());
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testReadLFHSizesAndDisk
    public void testReadLFHSizesAndDisk() throws ZipException {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField();
        byte[] b = new byte[20];
        System.arraycopy(SIZE.getBytes(), 0, b, 0, 8);
        System.arraycopy(CSIZE.getBytes(), 0, b, 8, 8);
        System.arraycopy(DISK.getBytes(), 0, b, 16, 4);
        f.parseFromLocalFileData(b, 0, b.length);
        assertEquals(SIZE, f.getSize());
        assertEquals(CSIZE, f.getCompressedSize());
        assertNull(f.getRelativeHeaderOffset());
        assertEquals(DISK, f.getDiskStartNumber());
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testReadCDSizesOffsetAndDisk
    public void testReadCDSizesOffsetAndDisk() throws ZipException {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField();
        byte[] b = new byte[28];
        System.arraycopy(SIZE.getBytes(), 0, b, 0, 8);
        System.arraycopy(CSIZE.getBytes(), 0, b, 8, 8);
        System.arraycopy(OFF.getBytes(), 0, b, 16, 8);
        System.arraycopy(DISK.getBytes(), 0, b, 24, 4);
        f.parseFromCentralDirectoryData(b, 0, b.length);
        assertEquals(SIZE, f.getSize());
        assertEquals(CSIZE, f.getCompressedSize());
        assertEquals(OFF, f.getRelativeHeaderOffset());
        assertEquals(DISK, f.getDiskStartNumber());
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testReadCDSizesAndOffset
    public void testReadCDSizesAndOffset() throws ZipException {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField();
        byte[] b = new byte[24];
        System.arraycopy(SIZE.getBytes(), 0, b, 0, 8);
        System.arraycopy(CSIZE.getBytes(), 0, b, 8, 8);
        System.arraycopy(OFF.getBytes(), 0, b, 16, 8);
        f.parseFromCentralDirectoryData(b, 0, b.length);
        assertEquals(SIZE, f.getSize());
        assertEquals(CSIZE, f.getCompressedSize());
        assertEquals(OFF, f.getRelativeHeaderOffset());
        assertNull(f.getDiskStartNumber());
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testReadCDSomethingAndDisk
    public void testReadCDSomethingAndDisk() throws ZipException {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField();
        byte[] b = new byte[12];
        System.arraycopy(SIZE.getBytes(), 0, b, 0, 8);
        System.arraycopy(DISK.getBytes(), 0, b, 8, 4);
        f.parseFromCentralDirectoryData(b, 0, b.length);
        assertNull(f.getSize());
        assertNull(f.getCompressedSize());
        assertNull(f.getRelativeHeaderOffset());
        assertEquals(DISK, f.getDiskStartNumber());
    }

// org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraFieldTest::testReparseCDSingleEightByteData
    public void testReparseCDSingleEightByteData() throws ZipException {
        Zip64ExtendedInformationExtraField f =
            new Zip64ExtendedInformationExtraField();
        byte[] b = new byte[8];
        System.arraycopy(SIZE.getBytes(), 0, b, 0, 8);
        f.parseFromCentralDirectoryData(b, 0, b.length);
        f.reparseCentralDirectoryData(true, false, false, false);
        assertEquals(SIZE, f.getSize());
        assertNull(f.getCompressedSize());
        assertNull(f.getRelativeHeaderOffset());
        assertNull(f.getDiskStartNumber());
        f.setSize(null);
        f.reparseCentralDirectoryData(false, true, false, false);
        assertNull(f.getSize());
        assertEquals(SIZE, f.getCompressedSize());
        assertNull(f.getRelativeHeaderOffset());
        assertNull(f.getDiskStartNumber());
        f.setCompressedSize(null);
        f.reparseCentralDirectoryData(false, false, true, false);
        assertNull(f.getSize());
        assertNull(f.getCompressedSize());
        assertEquals(SIZE, f.getRelativeHeaderOffset());
        assertNull(f.getDiskStartNumber());
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testExtraFields
    public void testExtraFields() {
        AsiExtraField a = new AsiExtraField();
        a.setDirectory(true);
        a.setMode(0755);
        UnrecognizedExtraField u = new UnrecognizedExtraField();
        u.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u.setLocalFileDataData(new byte[0]);

        ZipArchiveEntry ze = new ZipArchiveEntry("test/");
        ze.setExtraFields(new ZipExtraField[] {a, u});
        byte[] data1 = ze.getExtra();
        ZipExtraField[] result = ze.getExtraFields();
        assertEquals("first pass", 2, result.length);
        assertSame(a, result[0]);
        assertSame(u, result[1]);

        UnrecognizedExtraField u2 = new UnrecognizedExtraField();
        u2.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u2.setLocalFileDataData(new byte[] {1});

        ze.addExtraField(u2);
        byte[] data2 = ze.getExtra();
        result = ze.getExtraFields();
        assertEquals("second pass", 2, result.length);
        assertSame(a, result[0]);
        assertSame(u2, result[1]);
        assertEquals("length second pass", data1.length+1, data2.length);

        UnrecognizedExtraField u3 = new UnrecognizedExtraField();
        u3.setHeaderId(new ZipShort(2));
        u3.setLocalFileDataData(new byte[] {1});
        ze.addExtraField(u3);
        result = ze.getExtraFields();
        assertEquals("third pass", 3, result.length);

        ze.removeExtraField(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        byte[] data3 = ze.getExtra();
        result = ze.getExtraFields();
        assertEquals("fourth pass", 2, result.length);
        assertSame(a, result[0]);
        assertSame(u3, result[1]);
        assertEquals("length fourth pass", data2.length, data3.length);

        try {
            ze.removeExtraField(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
            fail("should be no such element");
        } catch (java.util.NoSuchElementException nse) {
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testExtraFieldMerging
    public void testExtraFieldMerging() {
        AsiExtraField a = new AsiExtraField();
        a.setDirectory(true);
        a.setMode(0755);
        UnrecognizedExtraField u = new UnrecognizedExtraField();
        u.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u.setLocalFileDataData(new byte[0]);

        ZipArchiveEntry ze = new ZipArchiveEntry("test/");
        ze.setExtraFields(new ZipExtraField[] {a, u});

        
        
        byte[] b = ExtraFieldUtilsTest.UNRECOGNIZED_HEADER.getBytes();
        ze.setCentralDirectoryExtra(new byte[] {b[0], b[1], 1, 0, 127});

        ZipExtraField[] result = ze.getExtraFields();
        assertEquals("first pass", 2, result.length);
        assertSame(a, result[0]);
        assertEquals(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER,
                     result[1].getHeaderId());
        assertEquals(new ZipShort(0), result[1].getLocalFileDataLength());
        assertEquals(new ZipShort(1), result[1].getCentralDirectoryLength());

        
        
        ze.setCentralDirectoryExtra(new byte[] {2, 0, 0, 0});

        result = ze.getExtraFields();
        assertEquals("second pass", 3, result.length);

        
        
        ze.setExtra(new byte[] {2, 0, 1, 0, 127});

        result = ze.getExtraFields();
        assertEquals("third pass", 3, result.length);
        assertSame(a, result[0]);
        assertEquals(new ZipShort(2), result[2].getHeaderId());
        assertEquals(new ZipShort(1), result[2].getLocalFileDataLength());
        assertEquals(new ZipShort(0), result[2].getCentralDirectoryLength());
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testAddAsFirstExtraField
    public void testAddAsFirstExtraField() {
        AsiExtraField a = new AsiExtraField();
        a.setDirectory(true);
        a.setMode(0755);
        UnrecognizedExtraField u = new UnrecognizedExtraField();
        u.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u.setLocalFileDataData(new byte[0]);

        ZipArchiveEntry ze = new ZipArchiveEntry("test/");
        ze.setExtraFields(new ZipExtraField[] {a, u});
        byte[] data1 = ze.getExtra();

        UnrecognizedExtraField u2 = new UnrecognizedExtraField();
        u2.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u2.setLocalFileDataData(new byte[] {1});

        ze.addAsFirstExtraField(u2);
        byte[] data2 = ze.getExtra();
        ZipExtraField[] result = ze.getExtraFields();
        assertEquals("second pass", 2, result.length);
        assertSame(u2, result[0]);
        assertSame(a, result[1]);
        assertEquals("length second pass", data1.length + 1, data2.length);

        UnrecognizedExtraField u3 = new UnrecognizedExtraField();
        u3.setHeaderId(new ZipShort(2));
        u3.setLocalFileDataData(new byte[] {1});
        ze.addAsFirstExtraField(u3);
        result = ze.getExtraFields();
        assertEquals("third pass", 3, result.length);
        assertSame(u3, result[0]);
        assertSame(u2, result[1]);
        assertSame(a, result[2]);
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testUnixMode
    public void testUnixMode() {
        ZipArchiveEntry ze = new ZipArchiveEntry("foo");
        assertEquals(0, ze.getPlatform());
        ze.setUnixMode(0755);
        assertEquals(3, ze.getPlatform());
        assertEquals(0755,
                     (ze.getExternalAttributes() >> 16) & 0xFFFF);
        assertEquals(0, ze.getExternalAttributes()  & 0xFFFF);

        ze.setUnixMode(0444);
        assertEquals(3, ze.getPlatform());
        assertEquals(0444,
                     (ze.getExternalAttributes() >> 16) & 0xFFFF);
        assertEquals(1, ze.getExternalAttributes()  & 0xFFFF);

        ze = new ZipArchiveEntry("foo/");
        assertEquals(0, ze.getPlatform());
        ze.setUnixMode(0777);
        assertEquals(3, ze.getPlatform());
        assertEquals(0777,
                     (ze.getExternalAttributes() >> 16) & 0xFFFF);
        assertEquals(0x10, ze.getExternalAttributes()  & 0xFFFF);

        ze.setUnixMode(0577);
        assertEquals(3, ze.getPlatform());
        assertEquals(0577,
                     (ze.getExternalAttributes() >> 16) & 0xFFFF);
        assertEquals(0x11, ze.getExternalAttributes()  & 0xFFFF);
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testCompressionMethod
    public void testCompressionMethod() throws Exception {
        ZipArchiveOutputStream zos =
            new ZipArchiveOutputStream(new ByteArrayOutputStream());
        ZipArchiveEntry entry = new ZipArchiveEntry("foo");
        assertEquals(-1, entry.getMethod());
        assertFalse(zos.canWriteEntryData(entry));

        entry.setMethod(ZipEntry.STORED);
        assertEquals(ZipEntry.STORED, entry.getMethod());
        assertTrue(zos.canWriteEntryData(entry));

        entry.setMethod(ZipEntry.DEFLATED);
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        assertTrue(zos.canWriteEntryData(entry));

        
        entry.setMethod(6);
        assertEquals(6, entry.getMethod());
        assertFalse(zos.canWriteEntryData(entry));
        zos.close();
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testNotEquals
    public void testNotEquals() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("bar");
        assertFalse(entry1.equals(entry2));
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testNullCommentEqualsEmptyComment
    public void testNullCommentEqualsEmptyComment() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry3 = new ZipArchiveEntry("foo");
        entry1.setComment(null);
        entry2.setComment("");
        entry3.setComment("bar");
        assertEquals(entry1, entry2);
        assertFalse(entry1.equals(entry3));
        assertFalse(entry2.equals(entry3));
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
            ZipArchiveEntry zae = zf.getEntry("USD0558682-20080101.ZIP");
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
        InputStream is = ZipArchiveInputStreamTest.class
            .getResourceAsStream("/archive_with_trailer.zip");
        ZipArchiveInputStream zip = new ZipArchiveInputStream(is);
        while (zip.getNextZipEntry() != null) {
            
        }
        byte[] expected = new byte[] {
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', '\n'
        };
        byte[] actual = new byte[expected.length];
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

// org.apache.commons.compress.archivers.zip.ZipFileTest::testCDOrder
    public void testCDOrder() throws Exception {
        readOrderTest();
        ArrayList<ZipArchiveEntry> l = Collections.list(zf.getEntries());
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
        ArrayList<ZipArchiveEntry> l = Collections.list(zf.getEntriesInPhysicalOrder());
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
        } catch (Exception ex) {
            fail("Caught exception of second close");
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testReadingOfStoredEntry
    public void testReadingOfStoredEntry() throws Exception {
        File f = File.createTempFile("commons-compress-zipfiletest", ".zip");
        f.deleteOnExit();
        OutputStream o = null;
        InputStream i = null;
        try {
            o = new FileOutputStream(f);
            ZipArchiveOutputStream zo = new ZipArchiveOutputStream(o);
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
            byte[] b = new byte[4];
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
        File archive = getFile("test-winzip.zip");
        zf = new ZipFile(archive);
        assertNull(zf.getEntry("\u00e4\\\u00fc.txt"));
        assertNotNull(zf.getEntry("\u00e4/\u00fc.txt"));
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testSkipsPK00Prefix
    public void testSkipsPK00Prefix() throws Exception {
        File archive = getFile("COMPRESS-208.zip");
        zf = new ZipFile(archive);
        assertNotNull(zf.getEntry("test1.xml"));
        assertNotNull(zf.getEntry("test2.xml"));
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testUnixSymlinkSampleFile
    public void testUnixSymlinkSampleFile() throws Exception {
        final String entryPrefix = "COMPRESS-214_unix_symlinks/";
        final TreeMap<String, String> expectedVals = new TreeMap<String, String>();

        
        expectedVals.put(entryPrefix + "link1", "../COMPRESS-214_unix_symlinks/./a/b/c/../../../\uF999");
        expectedVals.put(entryPrefix + "link2", "../COMPRESS-214_unix_symlinks/./a/b/c/../../../g");
        expectedVals.put(entryPrefix + "link3", "../COMPRESS-214_unix_symlinks/././a/b/c/../../../\u76F4\u6A39");
        expectedVals.put(entryPrefix + "link4", "\u82B1\u5B50/\u745B\u5B50");
        expectedVals.put(entryPrefix + "\uF999", "./\u82B1\u5B50/\u745B\u5B50/\u5897\u8C37/\uF999");
        expectedVals.put(entryPrefix + "g", "./a/b/c/d/e/f/g");
        expectedVals.put(entryPrefix + "\u76F4\u6A39", "./g");

        
        
        expectedVals.put(entryPrefix + "link5", "../COMPRESS-214_unix_symlinks/././a/b");
        expectedVals.put(entryPrefix + "link6", "../COMPRESS-214_unix_symlinks/././a/b/");

        
        

        File archive = getFile("COMPRESS-214_unix_symlinks.zip");

        zf = new ZipFile(archive);
        Enumeration<ZipArchiveEntry> en = zf.getEntries();
        while (en.hasMoreElements()) {
            ZipArchiveEntry zae = en.nextElement();
            String link = zf.getUnixSymlink(zae);
            if (zae.isUnixSymlink()) {
                String name = zae.getName();
                String expected = expectedVals.get(name);
                assertEquals(expected, link);
            } else {
                
                assertNull(link);
            }
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testDuplicateEntry
    public void testDuplicateEntry() throws Exception {
        File archive = getFile("COMPRESS-227.zip");
        zf = new ZipFile(archive);

        ZipArchiveEntry ze = zf.getEntry("test1.txt");
        assertNotNull(ze);
        assertNotNull(zf.getInputStream(ze));

        int numberOfEntries = 0;
        for (Iterator<ZipArchiveEntry> it = zf.getEntries("test1.txt");
             it.hasNext(); ) {
            numberOfEntries++;
            ze = it.next();
            assertNotNull(zf.getInputStream(ze));
        }
        assertEquals(2, numberOfEntries);
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testExcessDataInZip64ExtraField
    public void testExcessDataInZip64ExtraField() throws Exception {
        File archive = getFile("COMPRESS-228.zip");
        zf = new ZipFile(archive);
        

        ZipArchiveEntry ze = zf.getEntry("src/main/java/org/apache/commons/compress/archivers/zip/ZipFile.java");
        assertEquals(26101, ze.getSize());
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
