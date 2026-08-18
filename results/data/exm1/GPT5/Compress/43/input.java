// buggy code
    private void writeLocalFileHeader(final ZipArchiveEntry ze, final boolean phased) throws IOException {
        final boolean encodable = zipEncoding.canEncode(ze.getName());
        final ByteBuffer name = getName(ze);

        if (createUnicodeExtraFields != UnicodeExtraFieldPolicy.NEVER) {
            addUnicodeExtraFields(ze, encodable, name);
        }

        final long localHeaderStart = streamCompressor.getTotalBytesWritten();
        final byte[] localHeader = createLocalFileHeader(ze, name, encodable, phased, localHeaderStart);
        metaData.put(ze, new EntryMetaData(localHeaderStart, usesDataDescriptor(ze.getMethod())));
        entry.localDataStart = localHeaderStart + LFH_CRC_OFFSET; // At crc offset
        writeCounted(localHeader);
        entry.dataStart = streamCompressor.getTotalBytesWritten();
    }

    private byte[] createLocalFileHeader(final ZipArchiveEntry ze, final ByteBuffer name, final boolean encodable,
                                         final boolean phased, long archiveOffset) throws IOException {
        ResourceAlignmentExtraField oldAlignmentEx =
            (ResourceAlignmentExtraField) ze.getExtraField(ResourceAlignmentExtraField.ID);
        if (oldAlignmentEx != null) {
            ze.removeExtraField(ResourceAlignmentExtraField.ID);
        }

        int alignment = ze.getAlignment();
        if (alignment <= 0 && oldAlignmentEx != null) {
            alignment = oldAlignmentEx.getAlignment();
        }

        if (alignment > 1 || (oldAlignmentEx != null && !oldAlignmentEx.allowMethodChange())) {
            int oldLength = LFH_FILENAME_OFFSET +
                            name.limit() - name.position() +
                            ze.getLocalFileDataExtra().length;

            int padding = (int) ((-archiveOffset - oldLength - ZipExtraField.EXTRAFIELD_HEADER_SIZE
                            - ResourceAlignmentExtraField.BASE_SIZE) &
                            (alignment - 1));
            ze.addExtraField(new ResourceAlignmentExtraField(alignment,
                            oldAlignmentEx != null && oldAlignmentEx.allowMethodChange(), padding));
        }

        final byte[] extra = ze.getLocalFileDataExtra();
        final int nameLen = name.limit() - name.position();
        final int len = LFH_FILENAME_OFFSET + nameLen + extra.length;
        final byte[] buf = new byte[len];

        System.arraycopy(LFH_SIG,  0, buf, LFH_SIG_OFFSET, WORD);

        //store method in local variable to prevent multiple method calls
        final int zipMethod = ze.getMethod();
        final boolean dataDescriptor = usesDataDescriptor(zipMethod);

        putShort(versionNeededToExtract(zipMethod, hasZip64Extra(ze), dataDescriptor), buf, LFH_VERSION_NEEDED_OFFSET);

        final GeneralPurposeBit generalPurposeBit = getGeneralPurposeBits(!encodable && fallbackToUTF8, dataDescriptor);
        generalPurposeBit.encode(buf, LFH_GPB_OFFSET);

        // compression method
        putShort(zipMethod, buf, LFH_METHOD_OFFSET);

        ZipUtil.toDosTime(calendarInstance, ze.getTime(), buf, LFH_TIME_OFFSET);

        // CRC
        if (phased){
            putLong(ze.getCrc(), buf, LFH_CRC_OFFSET);
        } else if (zipMethod == DEFLATED || channel != null) {
            System.arraycopy(LZERO, 0, buf, LFH_CRC_OFFSET, WORD);
        } else {
            putLong(ze.getCrc(), buf, LFH_CRC_OFFSET);
        }

        // compressed length
        // uncompressed length
        if (hasZip64Extra(entry.entry)){
            // point to ZIP64 extended information extra field for
            // sizes, may get rewritten once sizes are known if
            // stream is seekable
            ZipLong.ZIP64_MAGIC.putLong(buf, LFH_COMPRESSED_SIZE_OFFSET);
            ZipLong.ZIP64_MAGIC.putLong(buf, LFH_ORIGINAL_SIZE_OFFSET);
        } else if (phased) {
            putLong(ze.getCompressedSize(), buf, LFH_COMPRESSED_SIZE_OFFSET);
            putLong(ze.getSize(), buf, LFH_ORIGINAL_SIZE_OFFSET);
        } else if (zipMethod == DEFLATED || channel != null) {
            System.arraycopy(LZERO, 0, buf, LFH_COMPRESSED_SIZE_OFFSET, WORD);
            System.arraycopy(LZERO, 0, buf, LFH_ORIGINAL_SIZE_OFFSET, WORD);
        } else { // Stored
            putLong(ze.getSize(), buf, LFH_COMPRESSED_SIZE_OFFSET);
            putLong(ze.getSize(), buf, LFH_ORIGINAL_SIZE_OFFSET);
        }
        // file name length
        putShort(nameLen, buf, LFH_FILENAME_LENGTH_OFFSET);

        // extra field length
        putShort(extra.length, buf, LFH_EXTRA_LENGTH_OFFSET);

        // file name
        System.arraycopy( name.array(), name.arrayOffset(), buf, LFH_FILENAME_OFFSET, nameLen);

        // extra fields
        System.arraycopy(extra, 0, buf, LFH_FILENAME_OFFSET + nameLen, extra.length);

        return buf;
    }

    protected void writeDataDescriptor(final ZipArchiveEntry ze) throws IOException {
        if (ze.getMethod() != DEFLATED || channel != null) {
            return;
        }
        writeCounted(DD_SIG);
        writeCounted(ZipLong.getBytes(ze.getCrc()));
        if (!hasZip64Extra(ze)) {
            writeCounted(ZipLong.getBytes(ze.getCompressedSize()));
            writeCounted(ZipLong.getBytes(ze.getSize()));
        } else {
            writeCounted(ZipEightByteInteger.getBytes(ze.getCompressedSize()));
            writeCounted(ZipEightByteInteger.getBytes(ze.getSize()));
        }
    }

    private boolean usesDataDescriptor(final int zipMethod) {
        return zipMethod == DEFLATED && channel == null;
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

// org.apache.commons.compress.ArchiveUtilsTest::testCompareBA
    public void testCompareBA(){
        final byte[] buffer1 = {1,2,3};
        final byte[] buffer2 = {1,2,3,0};
        final byte[] buffer3 = {1,2,3};
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
        final byte[] buffer1 = {'a','b','c'};
        final byte[] buffer2 = {'d','e','f',0};
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

// org.apache.commons.compress.ArchiveUtilsTest::sanitizeShortensString
    public void sanitizeShortensString() {
        final String input = "012345678901234567890123456789012345678901234567890123456789"
            + "012345678901234567890123456789012345678901234567890123456789"
            + "012345678901234567890123456789012345678901234567890123456789"
            + "012345678901234567890123456789012345678901234567890123456789"
            + "012345678901234567890123456789012345678901234567890123456789";
        final String expected = "012345678901234567890123456789012345678901234567890123456789"
            + "012345678901234567890123456789012345678901234567890123456789"
            + "012345678901234567890123456789012345678901234567890123456789"
            + "012345678901234567890123456789012345678901234567890123456789"
            + "012345678901...";
        assertEquals(expected, ArchiveUtils.sanitize(input));
    }

// org.apache.commons.compress.ArchiveUtilsTest::sanitizeLeavesShortStringsAlone
    public void sanitizeLeavesShortStringsAlone() {
        final String input = "012345678901234567890123456789012345678901234567890123456789";
        assertEquals(input, ArchiveUtils.sanitize(input));
    }

// org.apache.commons.compress.ArchiveUtilsTest::sanitizeRemovesUnprintableCharacters
    public void sanitizeRemovesUnprintableCharacters() {
        final String input = "\b12345678901234567890123456789012345678901234567890123456789";
        final String expected = "?12345678901234567890123456789012345678901234567890123456789";
        assertEquals(expected, ArchiveUtils.sanitize(input));
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
        try (final InputStream is = new FileInputStream(input);
                final ArchiveInputStream in = new ArchiveStreamFactory()
                        .createArchiveInputStream(new BufferedInputStream(is))) {
            final ArArchiveEntry entry = (ArArchiveEntry) in.getNextEntry();

            final File target = new File(dir, entry.getName());
            try (final OutputStream out = new FileOutputStream(target)) {
                IOUtils.copy(in, out);
            }
        }
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

// org.apache.commons.compress.archivers.ArTestCase::XtestDirectoryEntryFromFile
    public void XtestDirectoryEntryFromFile() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        ArArchiveOutputStream aos = null;
        ArArchiveInputStream ais = null;
        try {
            archive = File.createTempFile("test.", ".ar", tmp[0]);
            archive.deleteOnExit();
            aos = new ArArchiveOutputStream(new FileOutputStream(archive));
            final long beforeArchiveWrite = tmp[0].lastModified();
            final ArArchiveEntry in = new ArArchiveEntry(tmp[0], "foo");
            aos.putArchiveEntry(in);
            aos.closeArchiveEntry();
            aos.close();
            aos = null;
            ais = new ArArchiveInputStream(new FileInputStream(archive));
            final ArArchiveEntry out = ais.getNextArEntry();
            ais.close();
            ais = null;
            assertNotNull(out);
            assertEquals("foo/", out.getName());
            assertEquals(0, out.getSize());
            
            assertEquals(beforeArchiveWrite / 1000,
                         out.getLastModifiedDate().getTime() / 1000);
            assertTrue(out.isDirectory());
        } finally {
            if (ais != null) {
                ais.close();
            }
            if (aos != null) {
                aos.close();
            }
            tryHardToDelete(archive);
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.ArTestCase::XtestExplicitDirectoryEntry
    public void XtestExplicitDirectoryEntry() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        ArArchiveOutputStream aos = null;
        ArArchiveInputStream ais = null;
        try {
            archive = File.createTempFile("test.", ".ar", tmp[0]);
            archive.deleteOnExit();
            aos = new ArArchiveOutputStream(new FileOutputStream(archive));
            final long beforeArchiveWrite = tmp[0].lastModified();
            final ArArchiveEntry in = new ArArchiveEntry("foo", 0, 0, 0, 0,
                                                   tmp[1].lastModified() / 1000);
            aos.putArchiveEntry(in);
            aos.closeArchiveEntry();
            aos.close();
            aos = null;
            ais = new ArArchiveInputStream(new FileInputStream(archive));
            final ArArchiveEntry out = ais.getNextArEntry();
            ais.close();
            ais = null;
            assertNotNull(out);
            assertEquals("foo/", out.getName());
            assertEquals(0, out.getSize());
            assertEquals(beforeArchiveWrite / 1000,
                         out.getLastModifiedDate().getTime() / 1000);
            assertTrue(out.isDirectory());
        } finally {
            if (ais != null) {
                ais.close();
            }
            if (aos != null) {
                aos.close();
            }
            tryHardToDelete(archive);
            tryHardToDelete(tmp[1]);
            rmdir(tmp[0]);
        }
    }

// org.apache.commons.compress.archivers.ArTestCase::testFileEntryFromFile
    public void testFileEntryFromFile() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        ArArchiveOutputStream aos = null;
        ArArchiveInputStream ais = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".ar", tmp[0]);
            archive.deleteOnExit();
            aos = new ArArchiveOutputStream(new FileOutputStream(archive));
            final ArArchiveEntry in = new ArArchiveEntry(tmp[1], "foo");
            aos.putArchiveEntry(in);
            final byte[] b = new byte[(int) tmp[1].length()];
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
            final ArArchiveEntry out = ais.getNextArEntry();
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
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        ArArchiveOutputStream aos = null;
        ArArchiveInputStream ais = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".ar", tmp[0]);
            archive.deleteOnExit();
            aos = new ArArchiveOutputStream(new FileOutputStream(archive));
            final ArArchiveEntry in = new ArArchiveEntry("foo", tmp[1].length(),
                                                   0, 0, 0,
                                                   tmp[1].lastModified() / 1000);
            aos.putArchiveEntry(in);
            final byte[] b = new byte[(int) tmp[1].length()];
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
            final ArArchiveEntry out = ais.getNextArEntry();
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

        try (ArchiveOutputStream aos1 = factory.createArchiveOutputStream("zip", out1)) {
            aos1.putArchiveEntry(new ZipArchiveEntry("dummy"));
            aos1.closeArchiveEntry();
        }

        final ArchiveOutputStream finishTest;
        try (ArchiveOutputStream aos1 = factory.createArchiveOutputStream("jar", out1)) {
            finishTest = aos1;
            aos1.putArchiveEntry(new JarArchiveEntry("dummy"));
            aos1.closeArchiveEntry();
        }
        try {
            finishTest.finish();
            fail("finish() cannot follow close()");
        } catch (final IOException io) {
            
        }
        finishTest.close();
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
        try (FileInputStream fis = new FileInputStream("src/test/resources/testAIFF.aif")) {
            try (InputStream is = new BufferedInputStream(fis)) {
                new ArchiveStreamFactory().createArchiveInputStream(is);
                fail("created an input stream for a non-archive");
            } catch (final ArchiveException ae) {
                assertTrue(ae.getMessage().startsWith("No Archiver found"));
            }
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testCOMPRESS209
    public void testCOMPRESS209() throws Exception {
        try (FileInputStream fis = new FileInputStream("src/test/resources/testCompress209.doc")) {
            try (InputStream bis = new BufferedInputStream(fis)) {
                new ArchiveStreamFactory().createArchiveInputStream(bis);
                fail("created an input stream for a non-archive");
            } catch (final ArchiveException ae) {
                assertTrue(ae.getMessage().startsWith("No Archiver found"));
            }
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
        try (FileInputStream fis = new FileInputStream("src/test/resources/bla.7z")) {
            try (InputStream bis = new BufferedInputStream(fis)) {
                new ArchiveStreamFactory().createArchiveInputStream(bis);
                fail("Expected a StreamingNotSupportedException");
            } catch (final StreamingNotSupportedException ex) {
                assertEquals(ArchiveStreamFactory.SEVEN_Z, ex.getFormat());
            }
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::skipsPK00Prefix
    public void skipsPK00Prefix() throws Exception {
        try (FileInputStream fis = new FileInputStream("src/test/resources/COMPRESS-208.zip")) {
            try (InputStream bis = new BufferedInputStream(fis)) {
                try (ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(bis)) {
                    assertTrue(ais instanceof ZipArchiveInputStream);
                }
            }
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

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testDetect
    public void testDetect() throws Exception {
        for (String extension : new String[]{
                ArchiveStreamFactory.AR,
                ArchiveStreamFactory.ARJ,
                ArchiveStreamFactory.CPIO,
                ArchiveStreamFactory.DUMP,
                
 
                ArchiveStreamFactory.SEVEN_Z,
                ArchiveStreamFactory.TAR,
                ArchiveStreamFactory.ZIP
        }) {
            assertEquals(extension, detect("bla."+extension));
        }

        try {
            ArchiveStreamFactory.detect(new BufferedInputStream(new ByteArrayInputStream(new byte[0])));
            fail("shouldn't be able to detect empty stream");
        } catch (ArchiveException e) {
            assertEquals("No Archiver found for the stream signature", e.getMessage());
        }

        try {
            ArchiveStreamFactory.detect(null);
            fail("shouldn't be able to detect null stream");
        } catch (IllegalArgumentException e) {
            assertEquals("Stream must not be null.", e.getMessage());
        }

        try {
            ArchiveStreamFactory.detect(new BufferedInputStream(new MockEvilInputStream()));
            fail("Expected ArchiveException");
        } catch (ArchiveException e) {
            assertEquals("IOException while reading signature.", e.getMessage());
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testEncodingInputStreamAutodetect
    public void testEncodingInputStreamAutodetect() throws Exception {
        int failed = 0;
        for (int i = 1; i <= TESTS.length; i++) {
            final TestData test = TESTS[i - 1];
            try (final ArchiveInputStream ais = getInputStreamFor(test.testFile, test.fac)) {
                final String field = getField(ais, test.fieldName);
                if (!eq(test.expectedEncoding, field)) {
                    System.out.println("Failed test " + i + ". expected: " + test.expectedEncoding + " actual: " + field
                            + " type: " + test.type);
                    failed++;
                }
            }
        }
        if (failed > 0) {
            fail("Tests failed: " + failed + " out of " + TESTS.length);
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testEncodingInputStream
    public void testEncodingInputStream() throws Exception {
        int failed = 0;
        for (int i = 1; i <= TESTS.length; i++) {
            final TestData test = TESTS[i - 1];
            try (final ArchiveInputStream ais = getInputStreamFor(test.type, test.testFile, test.fac)) {
                final String field = getField(ais, test.fieldName);
                if (!eq(test.expectedEncoding, field)) {
                    System.out.println("Failed test " + i + ". expected: " + test.expectedEncoding + " actual: " + field
                            + " type: " + test.type);
                    failed++;
                }
            }
        }
        if (failed > 0) {
            fail("Tests failed: " + failed + " out of " + TESTS.length);
        }
    }

// org.apache.commons.compress.archivers.ArchiveStreamFactoryTest::testEncodingOutputStream
    public void testEncodingOutputStream() throws Exception {
        int failed = 0;
        for(int i = 1; i <= TESTS.length; i++) {
            final TestData test = TESTS[i-1];
            if (test.hasOutputStream) {
                try (final ArchiveOutputStream ais = getOutputStreamFor(test.type, test.fac)) {
                    final String field = getField(ais, test.fieldName);
                    if (!eq(test.expectedEncoding, field)) {
                        System.out.println("Failed test " + i + ". expected: " + test.expectedEncoding + " actual: "
                                + field + " type: " + test.type);
                        failed++;
                    }
                }
            }
        }
        if (failed > 0) {
            fail("Tests failed: " + failed + " out of " + TESTS.length);
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

        final Map<String, File> result = new HashMap<>();
        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            final File cpioget = new File(dir, entry.getName());
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
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        CpioArchiveOutputStream tos = null;
        CpioArchiveInputStream tis = null;
        try {
            archive = File.createTempFile("test.", ".cpio", tmp[0]);
            archive.deleteOnExit();
            tos = new CpioArchiveOutputStream(new FileOutputStream(archive));
            final long beforeArchiveWrite = tmp[0].lastModified();
            final CpioArchiveEntry in = new CpioArchiveEntry(tmp[0], "foo");
            tos.putArchiveEntry(in);
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new CpioArchiveInputStream(new FileInputStream(archive));
            final CpioArchiveEntry out = tis.getNextCPIOEntry();
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
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        CpioArchiveOutputStream tos = null;
        CpioArchiveInputStream tis = null;
        try {
            archive = File.createTempFile("test.", ".cpio", tmp[0]);
            archive.deleteOnExit();
            tos = new CpioArchiveOutputStream(new FileOutputStream(archive));
            final long beforeArchiveWrite = tmp[0].lastModified();
            final CpioArchiveEntry in = new CpioArchiveEntry("foo/");
            in.setTime(beforeArchiveWrite / 1000);
            in.setMode(CpioConstants.C_ISDIR);
            tos.putArchiveEntry(in);
            tos.closeArchiveEntry();
            tos.close();
            tos = null;
            tis = new CpioArchiveInputStream(new FileInputStream(archive));
            final CpioArchiveEntry out = tis.getNextCPIOEntry();
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
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        CpioArchiveOutputStream tos = null;
        CpioArchiveInputStream tis = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".cpio", tmp[0]);
            archive.deleteOnExit();
            tos = new CpioArchiveOutputStream(new FileOutputStream(archive));
            final CpioArchiveEntry in = new CpioArchiveEntry(tmp[1], "foo");
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
            tis = new CpioArchiveInputStream(new FileInputStream(archive));
            final CpioArchiveEntry out = tis.getNextCPIOEntry();
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
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        CpioArchiveOutputStream tos = null;
        CpioArchiveInputStream tis = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".cpio", tmp[0]);
            archive.deleteOnExit();
            tos = new CpioArchiveOutputStream(new FileOutputStream(archive));
            final CpioArchiveEntry in = new CpioArchiveEntry("foo");
            in.setTime(tmp[1].lastModified() / 1000);
            in.setSize(tmp[1].length());
            in.setMode(CpioConstants.C_ISREG);
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
            tis = new CpioArchiveInputStream(new FileInputStream(archive));
            final CpioArchiveEntry out = tis.getNextCPIOEntry();
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
        }catch (final IllegalArgumentException e) {
            assertEquals(ARCHIVER_NULL_MESSAGE, e.getMessage());
        } catch (final ArchiveException e) {
            fail("ArchiveException not expected");
        }
    }

// org.apache.commons.compress.archivers.ExceptionMessageTest::testMessageWhenInputStreamIsNull
    public void testMessageWhenInputStreamIsNull(){
        try{
            new ArchiveStreamFactory().createArchiveInputStream("zip", null);
            fail("Should raise an IllegalArgumentException.");
        }catch (final IllegalArgumentException e) {
            assertEquals(INPUTSTREAM_NULL_MESSAGE, e.getMessage());
        } catch (final ArchiveException e) {
            fail("ArchiveException not expected");
        }
    }

// org.apache.commons.compress.archivers.ExceptionMessageTest::testMessageWhenArchiverNameIsNull_2
    public void testMessageWhenArchiverNameIsNull_2(){
        try{
            new ArchiveStreamFactory().createArchiveOutputStream(null, System.out);
            fail("Should raise an IllegalArgumentException.");
        } catch (final IllegalArgumentException e) {
            assertEquals(ARCHIVER_NULL_MESSAGE, e.getMessage());
        } catch (final ArchiveException e){
            fail("ArchiveException not expected");
        }
    }

// org.apache.commons.compress.archivers.ExceptionMessageTest::testMessageWhenOutputStreamIsNull
    public void testMessageWhenOutputStreamIsNull(){
        try{
            new ArchiveStreamFactory().createArchiveOutputStream("zip", null);
            fail("Should raise an IllegalArgumentException.");
        } catch (final IllegalArgumentException e) {
            assertEquals(OUTPUTSTREAM_NULL_MESSAGE, e.getMessage());
        } catch (final ArchiveException e) {
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
            final File archiveEntry = new File(dir, entry.getName());
            archiveEntry.getParentFile().mkdirs();
            if(entry.isDirectory()){
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
        is.close();
    }

// org.apache.commons.compress.archivers.LongPathTest::testArchive
    public void testArchive() {}

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

// org.apache.commons.compress.archivers.SevenZTestCase::testSevenZArchiveCreationUsingLZMA
    public void testSevenZArchiveCreationUsingLZMA() throws Exception {
        testSevenZArchiveCreation(SevenZMethod.LZMA);
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

        
        final List<File> results = new ArrayList<>();

        final InputStream is = new FileInputStream(output);
        ArchiveInputStream in = null;
        try {
            in = new ArchiveStreamFactory()
                .createArchiveInputStream("zip", is);

            ZipArchiveEntry entry = null;
            while((entry = (ZipArchiveEntry)in.getNextEntry()) != null) {
                final File outfile = new File(resultDir.getCanonicalPath() + "/result/" + entry.getName());
                outfile.getParentFile().mkdirs();
                try (OutputStream o = new FileOutputStream(outfile)) {
                    IOUtils.copy(in, o);
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

// org.apache.commons.compress.archivers.ZipTestCase::testZipArchiveCreationInMemory
    public void testZipArchiveCreationInMemory() throws Exception {
        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");
        final byte[] file1Contents = new byte[(int) file1.length()];
        final byte[] file2Contents = new byte[(int) file2.length()];
        IOUtils.readFully(new FileInputStream(file1), file1Contents);
        IOUtils.readFully(new FileInputStream(file2), file2Contents);

        SeekableInMemoryByteChannel c = new SeekableInMemoryByteChannel();
        try (ZipArchiveOutputStream os = new ZipArchiveOutputStream(c)) {
            os.putArchiveEntry(new ZipArchiveEntry("testdata/test1.xml"));
            os.write(file1Contents);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ZipArchiveEntry("testdata/test2.xml"));
            os.write(file2Contents);
            os.closeArchiveEntry();
        }

        
        final List<byte[]> results = new ArrayList<>();

        try (ArchiveInputStream in = new ArchiveStreamFactory()
             .createArchiveInputStream("zip", new ByteArrayInputStream(c.array()))) {

            ZipArchiveEntry entry;
            while((entry = (ZipArchiveEntry)in.getNextEntry()) != null) {
                byte[] result = new byte[(int) entry.getSize()];
                IOUtils.readFully(in, result);
                results.add(result);
            }
        }

        assertArrayEquals(results.get(0), file1Contents);
        assertArrayEquals(results.get(1), file2Contents);
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
        final ArrayList<String> al = new ArrayList<>();
        al.add("test1.xml");
        al.add("test2.xml");
        try (InputStream is = new FileInputStream(input)) {
            checkArchiveContent(new ZipArchiveInputStream(is), al);
        }
    }

// org.apache.commons.compress.archivers.ZipTestCase::testSupportedCompressionMethod
    public void testSupportedCompressionMethod() throws IOException {
        
        
        final ZipFile moby = new ZipFile(getFile("moby.zip"));
        final ZipArchiveEntry entry = moby.getEntry("README");
        assertEquals("method", ZipMethod.TOKENIZATION.getCode(), entry.getMethod());
        assertFalse(moby.canReadEntryData(entry));
        moby.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testSkipEntryWithUnsupportedCompressionMethod
    public void testSkipEntryWithUnsupportedCompressionMethod()
            throws IOException {
        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(new FileInputStream(getFile("moby.zip")))) {
            final ZipArchiveEntry entry = zip.getNextZipEntry();
            assertEquals("method", ZipMethod.TOKENIZATION.getCode(), entry.getMethod());
            assertEquals("README", entry.getName());
            assertFalse(zip.canReadEntryData(entry));
            try {
                assertNull(zip.getNextZipEntry());
            } catch (final IOException e) {
                e.printStackTrace();
                fail("COMPRESS-93: Unable to skip an unsupported zip entry");
            }
        }
    }

// org.apache.commons.compress.archivers.ZipTestCase::testListAllFilesWithNestedArchive
    public void testListAllFilesWithNestedArchive() throws Exception {
        final File input = getFile("OSX_ArchiveWithNestedArchive.zip");

        final List<String> results = new ArrayList<>();
        final List<ZipException> expectedExceptions = new ArrayList<>();

        final InputStream is = new FileInputStream(input);
        ArchiveInputStream in = null;
        try {
            in = new ArchiveStreamFactory().createArchiveInputStream("zip", is);

            ZipArchiveEntry entry = null;
            while ((entry = (ZipArchiveEntry) in.getNextEntry()) != null) {
                results.add(entry.getName());

                final ArchiveInputStream nestedIn = new ArchiveStreamFactory().createArchiveInputStream("zip", in);
                try {
                    ZipArchiveEntry nestedEntry = null;
                    while ((nestedEntry = (ZipArchiveEntry) nestedIn.getNextEntry()) != null) {
                        results.add(nestedEntry.getName());
                    }
                } catch (ZipException ex) {
                    
                    expectedExceptions.add(ex);
                }
                
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        is.close();

        assertTrue(results.contains("NestedArchiv.zip"));
        assertTrue(results.contains("test1.xml"));
        assertTrue(results.contains("test2.xml"));
        assertTrue(results.contains("test3.xml"));
        assertEquals(1, expectedExceptions.size());
    }

// org.apache.commons.compress.archivers.ZipTestCase::testDirectoryEntryFromFile
    public void testDirectoryEntryFromFile() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        ZipArchiveOutputStream zos = null;
        ZipFile zf = null;
        try {
            archive = File.createTempFile("test.", ".zip", tmp[0]);
            archive.deleteOnExit();
            zos = new ZipArchiveOutputStream(archive);
            final long beforeArchiveWrite = tmp[0].lastModified();
            final ZipArchiveEntry in = new ZipArchiveEntry(tmp[0], "foo");
            zos.putArchiveEntry(in);
            zos.closeArchiveEntry();
            zos.close();
            zos = null;
            zf = new ZipFile(archive);
            final ZipArchiveEntry out = zf.getEntry("foo/");
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
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        ZipArchiveOutputStream zos = null;
        ZipFile zf = null;
        try {
            archive = File.createTempFile("test.", ".zip", tmp[0]);
            archive.deleteOnExit();
            zos = new ZipArchiveOutputStream(archive);
            final long beforeArchiveWrite = tmp[0].lastModified();
            final ZipArchiveEntry in = new ZipArchiveEntry("foo/");
            in.setTime(beforeArchiveWrite);
            zos.putArchiveEntry(in);
            zos.closeArchiveEntry();
            zos.close();
            zos = null;
            zf = new ZipFile(archive);
            final ZipArchiveEntry out = zf.getEntry("foo/");
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

// org.apache.commons.compress.archivers.ZipTestCase::test
        public boolean test(final ZipArchiveEntry zipArchiveEntry) {
            return true;
        }

// org.apache.commons.compress.archivers.ZipTestCase::testCopyRawEntriesFromFile
    public void testCopyRawEntriesFromFile()
            throws IOException {

        final File[] tmp = createTempDirAndFile();
        final File reference = createReferenceFile(tmp[0], Zip64Mode.Never, "expected.");

        final File a1 = File.createTempFile("src1.", ".zip", tmp[0]);
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(a1);
        zos.setUseZip64(Zip64Mode.Never);
        createFirstEntry(zos).close();

        final File a2 = File.createTempFile("src2.", ".zip", tmp[0]);
        final ZipArchiveOutputStream zos1 = new ZipArchiveOutputStream(a2);
        zos1.setUseZip64(Zip64Mode.Never);
        createSecondEntry(zos1).close();

        final ZipFile zf1 = new ZipFile(a1);
        final ZipFile zf2 = new ZipFile(a2);
        final File fileResult = File.createTempFile("file-actual.", ".zip", tmp[0]);
        final ZipArchiveOutputStream zos2 = new ZipArchiveOutputStream(fileResult);
        zf1.copyRawEntries(zos2, allFilesPredicate);
        zf2.copyRawEntries(zos2, allFilesPredicate);
        zos2.close();
        
        
        
        assertSameFileContents(reference, fileResult);
        zf1.close();
        zf2.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testCopyRawZip64EntryFromFile
    public void testCopyRawZip64EntryFromFile()
            throws IOException {

        final File[] tmp = createTempDirAndFile();
        final File reference = File.createTempFile("z64reference.", ".zip", tmp[0]);
        final ZipArchiveOutputStream zos1 = new ZipArchiveOutputStream(reference);
        zos1.setUseZip64(Zip64Mode.Always);
        createFirstEntry(zos1);
        zos1.close();

        final File a1 = File.createTempFile("zip64src.", ".zip", tmp[0]);
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(a1);
        zos.setUseZip64(Zip64Mode.Always);
        createFirstEntry(zos).close();

        final ZipFile zf1 = new ZipFile(a1);
        final File fileResult = File.createTempFile("file-actual.", ".zip", tmp[0]);
        final ZipArchiveOutputStream zos2 = new ZipArchiveOutputStream(fileResult);
        zos2.setUseZip64(Zip64Mode.Always);
        zf1.copyRawEntries(zos2, allFilesPredicate);
        zos2.close();
        assertSameFileContents(reference, fileResult);
        zf1.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testUnixModeInAddRaw
    public void testUnixModeInAddRaw() throws IOException {

        final File[] tmp = createTempDirAndFile();

        final File a1 = File.createTempFile("unixModeBits.", ".zip", tmp[0]);
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(a1);

        final ZipArchiveEntry archiveEntry = new ZipArchiveEntry("fred");
        archiveEntry.setUnixMode(0664);
        archiveEntry.setMethod(ZipEntry.DEFLATED);
        zos.addRawArchiveEntry(archiveEntry, new ByteArrayInputStream("fud".getBytes()));
        zos.close();

        final ZipFile zf1 = new ZipFile(a1);
        final ZipArchiveEntry fred = zf1.getEntry("fred");
        assertEquals(0664, fred.getUnixMode());
        zf1.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testFileEntryFromFile
    public void testFileEntryFromFile() throws Exception {
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        ZipArchiveOutputStream zos = null;
        ZipFile zf = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".zip", tmp[0]);
            archive.deleteOnExit();
            zos = new ZipArchiveOutputStream(archive);
            final ZipArchiveEntry in = new ZipArchiveEntry(tmp[1], "foo");
            zos.putArchiveEntry(in);
            final byte[] b = new byte[(int) tmp[1].length()];
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
            final ZipArchiveEntry out = zf.getEntry("foo");
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
        final File[] tmp = createTempDirAndFile();
        File archive = null;
        ZipArchiveOutputStream zos = null;
        ZipFile zf = null;
        FileInputStream fis = null;
        try {
            archive = File.createTempFile("test.", ".zip", tmp[0]);
            archive.deleteOnExit();
            zos = new ZipArchiveOutputStream(archive);
            final ZipArchiveEntry in = new ZipArchiveEntry("foo");
            in.setTime(tmp[1].lastModified());
            in.setSize(tmp[1].length());
            zos.putArchiveEntry(in);
            final byte[] b = new byte[(int) tmp[1].length()];
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
            final ZipArchiveEntry out = zf.getEntry("foo");
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
        try (ArArchiveOutputStream os = new ArArchiveOutputStream(new ByteArrayOutputStream())) {
            final ArArchiveEntry ae = new ArArchiveEntry("this_is_a_long_name.txt", 0);
            os.putArchiveEntry(ae);
            fail("Expected an exception");
        } catch (final IOException ex) {
            assertTrue(ex.getMessage().startsWith("filename too long"));
        }
    }

// org.apache.commons.compress.archivers.ar.ArArchiveOutputStreamTest::testLongFileNamesWorkUsingBSDDialect
    public void testLongFileNamesWorkUsingBSDDialect() throws Exception {
        final File[] df = createTempDirAndFile();
        try (FileOutputStream fos = new FileOutputStream(df[1]);
                ArArchiveOutputStream os = new ArArchiveOutputStream(fos)) {
            os.setLongFileMode(ArArchiveOutputStream.LONGFILE_BSD);
            final ArArchiveEntry ae = new ArArchiveEntry("this_is_a_long_name.txt", 14);
            os.putArchiveEntry(ae);
            os.write(new byte[] { 'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', '\n' });
            os.closeArchiveEntry();

            final List<String> expected = new ArrayList<>();
            expected.add("this_is_a_long_name.txt");
            checkArchiveContent(df[1], expected);
        } finally {
            rmdir(df[0]);
        }
    }

// org.apache.commons.compress.archivers.arj.ArjArchiveInputStreamTest::testArjUnarchive
    public void testArjUnarchive() throws Exception {
        final StringBuilder expected = new StringBuilder();
        expected.append("test1.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>test2.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>\n");

        final ArjArchiveInputStream in = new ArjArchiveInputStream(new FileInputStream(getFile("bla.arj")));
        ArjArchiveEntry entry;

        final StringBuilder result = new StringBuilder();
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
        final ArjArchiveInputStream in = new ArjArchiveInputStream(new FileInputStream(getFile("bla.arj")));
        final ArjArchiveEntry entry = in.getNextEntry();
        assertEquals("test1.xml", entry.getName());
        assertEquals(30, entry.getSize());
        assertEquals(0, entry.getUnixMode());
        final Calendar cal = Calendar.getInstance();
        cal.set(2008, 9, 6, 23, 50, 52);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), entry.getLastModifiedDate());
        in.close();
    }

// org.apache.commons.compress.archivers.arj.ArjArchiveInputStreamTest::testReadingOfAttributesUnixVersion
    public void testReadingOfAttributesUnixVersion() throws Exception {
        final ArjArchiveInputStream in = new ArjArchiveInputStream(new FileInputStream(getFile("bla.unix.arj")));
        final ArjArchiveEntry entry = in.getNextEntry();
        assertEquals("test1.xml", entry.getName());
        assertEquals(30, entry.getSize());
        assertEquals(0664, entry.getUnixMode() & 07777 );
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0000"));
        cal.set(2008, 9, 6, 21, 50, 52);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), entry.getLastModifiedDate());
        in.close();
    }

// org.apache.commons.compress.archivers.cpio.CpioArchiveInputStreamTest::testCpioUnarchive
    public void testCpioUnarchive() throws Exception {
        final StringBuilder expected = new StringBuilder();
        expected.append("./test1.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>./test2.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>\n");

        final CpioArchiveInputStream in = new CpioArchiveInputStream(new FileInputStream(getFile("bla.cpio")));
        CpioArchiveEntry entry;

        final StringBuilder result = new StringBuilder();
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
        final CpioArchiveInputStream in =
            new CpioArchiveInputStream(new FileInputStream(getFile("redline.cpio")));
        CpioArchiveEntry entry= null;

        int count = 0;
        while ((entry = (CpioArchiveEntry) in.getNextEntry()) != null) {
            count++;
            assertNotNull(entry);
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
            final CpioArchiveEntry e = ((CpioArchiveInputStream) in)
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
        try (FileInputStream is = new FileInputStream(getFile("bla.zip"))) {
            new DumpArchiveInputStream(is).close();
            fail("expected an exception");
        } catch (final ArchiveException ex) {
            
            assertTrue(ex.getCause() instanceof ShortFileException);
        }
    }

// org.apache.commons.compress.archivers.dump.DumpArchiveInputStreamTest::testNotADumpArchiveButBigEnough
    public void testNotADumpArchiveButBigEnough() throws Exception {
        try (FileInputStream is = new FileInputStream(getFile("zip64support.tar.bz2"))) {
            new DumpArchiveInputStream(is).close();
            fail("expected an exception");
        } catch (final ArchiveException ex) {
            
            assertTrue(ex.getCause() instanceof UnrecognizedFormatException);
        }
    }

// org.apache.commons.compress.archivers.dump.DumpArchiveInputStreamTest::testConsumesArchiveCompletely
    public void testConsumesArchiveCompletely() throws Exception {
        final InputStream is = DumpArchiveInputStreamTest.class
            .getResourceAsStream("/archive_with_trailer.dump");
        final DumpArchiveInputStream dump = new DumpArchiveInputStream(is);
        while (dump.getNextDumpEntry() != null) {
            
        }
        final byte[] expected = new byte[] {
            'H', 'e', 'l', 'l', 'o', ',', ' ', 'w', 'o', 'r', 'l', 'd', '!', '\n'
        };
        final byte[] actual = new byte[expected.length];
        is.read(actual);
        assertArrayEquals(expected, actual);
        dump.close();
    }

// org.apache.commons.compress.archivers.jar.JarArchiveOutputStreamTest::testJarMarker
    public void testJarMarker() throws IOException {
        final File testArchive = File.createTempFile("jar-aostest", ".jar");
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
                } catch (final IOException e) {  }
            }
            ZipFile.closeQuietly(zf);
            AbstractTestCase.tryHardToDelete(testArchive);
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testRandomlySkippingEntries
    public void testRandomlySkippingEntries() throws Exception {
        
        final Map<String, byte[]> entriesByName = new HashMap<>();
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
        try (SevenZFile archive = new SevenZFile(getFile("7z-empty-mhc-off.7z"))) {
            assertNotNull(archive.getNextEntry());
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
            new SevenZFile(getFile("bla.encrypted.7z")).close();
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
        try (SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-256.7z"))) {
            int count = 0;
            while (sevenZFile.getNextEntry() != null) {
                count++;
            }
            assertEquals(446, count);
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
        try (SevenZOutputFile outArchive = new SevenZOutputFile(output)) {
            outArchive.setContentMethods(Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.LZMA2, 1 << 20)));
            final SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo.txt");
            outArchive.putArchiveEntry(entry);
            outArchive.write(new byte[] { 'A' });
            outArchive.closeArchiveEntry();
        }

        try (SevenZFile archive = new SevenZFile(output)) {
            final SevenZArchiveEntry entry = archive.getNextEntry();
            final SevenZMethodConfiguration m = entry.getContentMethods().iterator().next();
            assertEquals(SevenZMethod.LZMA2, m.getMethod());
            assertEquals(1 << 20, m.getOptions());
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::testReadingBackDeltaDistance
    public void testReadingBackDeltaDistance() throws Exception {
        final File output = new File(dir, "delta-distance.7z");
        try (SevenZOutputFile outArchive = new SevenZOutputFile(output)) {
            outArchive.setContentMethods(Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.DELTA_FILTER, 32),
                    new SevenZMethodConfiguration(SevenZMethod.LZMA2)));
            final SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo.txt");
            outArchive.putArchiveEntry(entry);
            outArchive.write(new byte[] { 'A' });
            outArchive.closeArchiveEntry();
        }

        try (SevenZFile archive = new SevenZFile(output)) {
            final SevenZArchiveEntry entry = archive.getNextEntry();
            final SevenZMethodConfiguration m = entry.getContentMethods().iterator().next();
            assertEquals(SevenZMethod.DELTA_FILTER, m.getMethod());
            assertEquals(32, m.getOptions());
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::getEntriesOfUnarchiveTest
    public void getEntriesOfUnarchiveTest() throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(getFile("bla.7z"))) {
            final Iterable<SevenZArchiveEntry> entries = sevenZFile.getEntries();
            final Iterator<SevenZArchiveEntry> iter = entries.iterator();
            SevenZArchiveEntry entry = iter.next();
            assertEquals("test1.xml", entry.getName());
            entry = iter.next();
            assertEquals("test2.xml", entry.getName());
            assertFalse(iter.hasNext());
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::getEntriesOfUnarchiveInMemoryTest
    public void getEntriesOfUnarchiveInMemoryTest() throws IOException {
        byte[] data = null;
        try (FileInputStream fis = new FileInputStream(getFile("bla.7z"))) {
            data = IOUtils.toByteArray(fis);
        }
        try (SevenZFile sevenZFile = new SevenZFile(new SeekableInMemoryByteChannel(data))) {
            final Iterable<SevenZArchiveEntry> entries = sevenZFile.getEntries();
            final Iterator<SevenZArchiveEntry> iter = entries.iterator();
            SevenZArchiveEntry entry = iter.next();
            assertEquals("test1.xml", entry.getName());
            entry = iter.next();
            assertEquals("test2.xml", entry.getName());
            assertFalse(iter.hasNext());
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZFileTest::readEntriesOfSize0
    public void readEntriesOfSize0() throws IOException {
        try (SevenZFile sevenZFile = new SevenZFile(getFile("COMPRESS-348.7z"))) {
            int entries = 0;
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            while (entry != null) {
                entries++;
                final int b = sevenZFile.read();
                if ("2.txt".equals(entry.getName()) || "5.txt".equals(entry.getName())) {
                    assertEquals(-1, b);
                } else {
                    assertNotEquals(-1, b);
                }
                entry = sevenZFile.getNextEntry();
            }
            assertEquals(5, entries);
        }
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDirectoriesAndEmptyFiles
    public void testDirectoriesAndEmptyFiles() throws Exception {
        output = new File(dir, "empties.7z");

        final Date accessDate = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        final Date creationDate = cal.getTime();

        try (SevenZOutputFile outArchive = new SevenZOutputFile(output)) {
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
        }

        try (SevenZFile archive = new SevenZFile(output)) {
            SevenZArchiveEntry entry = archive.getNextEntry();
            assert (entry != null);
            assertEquals("foo/", entry.getName());
            assertTrue(entry.isDirectory());
            assertFalse(entry.isAntiItem());

            entry = archive.getNextEntry();
            assert (entry != null);
            assertEquals("foo/bar", entry.getName());
            assertFalse(entry.isDirectory());
            assertFalse(entry.isAntiItem());
            assertEquals(0, entry.getSize());
            assertFalse(entry.getHasLastModifiedDate());
            assertEquals(accessDate, entry.getAccessDate());
            assertEquals(creationDate, entry.getCreationDate());

            entry = archive.getNextEntry();
            assert (entry != null);
            assertEquals("xyzzy", entry.getName());
            assertEquals(1, entry.getSize());
            assertFalse(entry.getHasAccessDate());
            assertFalse(entry.getHasCreationDate());
            assertEquals(0, archive.read());

            entry = archive.getNextEntry();
            assert (entry != null);
            assertEquals("baz/", entry.getName());
            assertTrue(entry.isDirectory());
            assertTrue(entry.isAntiItem());

            entry = archive.getNextEntry();
            assert (entry != null);
            assertEquals("dada", entry.getName());
            assertEquals(2, entry.getSize());
            final byte[] content = new byte[2];
            assertEquals(2, archive.read(content));
            assertEquals(5, content[0]);
            assertEquals(42, content[1]);
            assertEquals(17, entry.getWindowsAttributes());

            assert (archive.getNextEntry() == null);
        }

    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testDirectoriesOnly
    public void testDirectoriesOnly() throws Exception {
        output = new File(dir, "dirs.7z");
        try (SevenZOutputFile outArchive = new SevenZOutputFile(output)) {
            final SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("foo/");
            entry.setDirectory(true);
            outArchive.putArchiveEntry(entry);
            outArchive.closeArchiveEntry();
        }

        try (SevenZFile archive = new SevenZFile(output)) {
            final SevenZArchiveEntry entry = archive.getNextEntry();
            assert (entry != null);
            assertEquals("foo/", entry.getName());
            assertTrue(entry.isDirectory());
            assertFalse(entry.isAntiItem());

            assert (archive.getNextEntry() == null);
        }

    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testCantFinishTwice
    public void testCantFinishTwice() throws Exception {
        output = new File(dir, "finish.7z");
        try (SevenZOutputFile outArchive = new SevenZOutputFile(output)) {
            outArchive.finish();
            outArchive.finish();
            fail("shouldn't be able to call finish twice");
        } catch (final IOException ex) {
            assertEquals("This archive has already been finished", ex.getMessage());
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
        final ArrayList<SevenZMethodConfiguration> methods = new ArrayList<>();
        methods.add(new SevenZMethodConfiguration(SevenZMethod.LZMA2));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.COPY));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.DEFLATE));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.BZIP2));
        createAndReadBack(output, methods);
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testStackOfContentCompressionsInMemory
    public void testStackOfContentCompressionsInMemory() throws Exception {
        final ArrayList<SevenZMethodConfiguration> methods = new ArrayList<>();
        methods.add(new SevenZMethodConfiguration(SevenZMethod.LZMA2));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.COPY));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.DEFLATE));
        methods.add(new SevenZMethodConfiguration(SevenZMethod.BZIP2));
        createAndReadBack(new SeekableInMemoryByteChannel(), methods);
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

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testLzmaWithIntConfiguration
    public void testLzmaWithIntConfiguration() throws Exception {
        output = new File(dir, "lzma-options.7z");
        
        createAndReadBack(output, Collections
                          .singletonList(new SevenZMethodConfiguration(SevenZMethod.LZMA, 1 << 20)));
    }

// org.apache.commons.compress.archivers.sevenz.SevenZOutputFileTest::testLzmaWithOptionsConfiguration
    public void testLzmaWithOptionsConfiguration() throws Exception {
        output = new File(dir, "lzma-options2.7z");
        final LZMA2Options opts = new LZMA2Options(1);
        createAndReadBack(output, Collections
                          .singletonList(new SevenZMethodConfiguration(SevenZMethod.LZMA, opts)));
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
        try (SevenZOutputFile outArchive = new SevenZOutputFile(output)) {
            addFile(outArchive, 0, true);
            addFile(outArchive, 1, true, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.BZIP2)));
        }

        try (SevenZFile archive = new SevenZFile(output)) {
            assertEquals(Boolean.TRUE,
                    verifyFile(archive, 0, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.LZMA2))));
            assertEquals(Boolean.TRUE,
                    verifyFile(archive, 1, Arrays.asList(new SevenZMethodConfiguration(SevenZMethod.BZIP2))));
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
        final Map<String, String> m = new HashMap<>();
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
        final Map<String, String> m = new HashMap<>();
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
        final Map<String, String> m = new HashMap<>();
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

// org.apache.commons.compress.archivers.zip.DataDescriptorTest::writesDataDescriptorForDeflatedEntryOnUnseekableOutput
    public void writesDataDescriptorForDeflatedEntryOnUnseekableOutput() throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(o)) {
            zos.putArchiveEntry(new ZipArchiveEntry("test1.txt"));
            zos.write("foo".getBytes("UTF-8"));
            zos.closeArchiveEntry();
        }
        byte[] data = o.toByteArray();

        byte[] versionInLFH = Arrays.copyOfRange(data, 4, 6);
        
        assertArrayEquals(new byte[] { 20, 0 }, versionInLFH);
        byte[] gpbInLFH = Arrays.copyOfRange(data, 6, 8);
        
        assertArrayEquals(new byte[] { 8, 8 }, gpbInLFH);
        byte[] crcAndSizedInLFH = Arrays.copyOfRange(data, 14, 26);
        assertArrayEquals(new byte[12], crcAndSizedInLFH);

        int cdhStart = findCentralDirectory(data);
        byte[] versionInCDH = Arrays.copyOfRange(data, cdhStart + 6, cdhStart + 8);
        assertArrayEquals(new byte[] { 20, 0 }, versionInCDH);
        byte[] gpbInCDH = Arrays.copyOfRange(data, cdhStart + 8, cdhStart + 10);
        assertArrayEquals(new byte[] { 8, 8 }, gpbInCDH);

        int ddStart = cdhStart - 16;
        assertEquals(ZipLong.DD_SIG, new ZipLong(data, ddStart));
        long crcFromDD = ZipLong.getValue(data, ddStart + 4);
        long cSizeFromDD = ZipLong.getValue(data, ddStart + 8);
        long sizeFromDD = ZipLong.getValue(data, ddStart + 12);
        assertEquals(3, sizeFromDD);

        long crcFromCDH = ZipLong.getValue(data, cdhStart + 16);
        assertEquals(crcFromDD, crcFromCDH);
        long cSizeFromCDH = ZipLong.getValue(data, cdhStart + 20);
        assertEquals(cSizeFromDD, cSizeFromCDH);
        long sizeFromCDH = ZipLong.getValue(data, cdhStart + 24);
        assertEquals(sizeFromDD, sizeFromCDH);
    }

// org.apache.commons.compress.archivers.zip.DataDescriptorTest::doesntWriteDataDescriptorForDeflatedEntryOnSeekableOutput
    public void doesntWriteDataDescriptorForDeflatedEntryOnSeekableOutput() throws IOException {
        File f = new File(dir, "test.zip");
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(f)) {
            zos.putArchiveEntry(new ZipArchiveEntry("test1.txt"));
            zos.write("foo".getBytes("UTF-8"));
            zos.closeArchiveEntry();
        }

        byte[] data;
        try (FileInputStream fis = new FileInputStream(f)) {
            data = IOUtils.toByteArray(fis);
        }

        byte[] versionInLFH = Arrays.copyOfRange(data, 4, 6);
        
        assertArrayEquals(new byte[] { 20, 0 }, versionInLFH);
        byte[] gpbInLFH = Arrays.copyOfRange(data, 6, 8);
        
        assertArrayEquals(new byte[] { 0, 8 }, gpbInLFH);

        int cdhStart = findCentralDirectory(data);
        byte[] versionInCDH = Arrays.copyOfRange(data, cdhStart + 6, cdhStart + 8);
        assertArrayEquals(new byte[] { 20, 0 }, versionInCDH);
        byte[] gpbInCDH = Arrays.copyOfRange(data, cdhStart + 8, cdhStart + 10);
        assertArrayEquals(new byte[] { 0, 8 }, gpbInCDH);

        int ddStart = cdhStart - 16;
        assertNotEquals(ZipLong.DD_SIG, new ZipLong(data, ddStart));
        long crcFromLFH = ZipLong.getValue(data, 14);
        long cSizeFromLFH = ZipLong.getValue(data, 18);
        long sizeFromLFH = ZipLong.getValue(data, 22);
        assertEquals(3, sizeFromLFH);

        long crcFromCDH = ZipLong.getValue(data, cdhStart + 16);
        assertEquals(crcFromLFH, crcFromCDH);
        long cSizeFromCDH = ZipLong.getValue(data, cdhStart + 20);
        assertEquals(cSizeFromLFH, cSizeFromCDH);
        long sizeFromCDH = ZipLong.getValue(data, cdhStart + 24);
        assertEquals(sizeFromLFH, sizeFromCDH);
    }

// org.apache.commons.compress.archivers.zip.DataDescriptorTest::doesntWriteDataDescriptorWhenAddingRawEntries
    public void doesntWriteDataDescriptorWhenAddingRawEntries() throws IOException {
        ByteArrayOutputStream init = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(init)) {
            zos.putArchiveEntry(new ZipArchiveEntry("test1.txt"));
            zos.write("foo".getBytes("UTF-8"));
            zos.closeArchiveEntry();
        }

        File f = new File(dir, "test.zip");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(init.toByteArray());
        }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ZipArchiveEntry zae;
        try (ZipFile zf = new ZipFile(f);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(o)) {
            zae = zf.getEntry("test1.txt");
            zos.addRawArchiveEntry(zae, zf.getRawInputStream(zae));
        }

        byte[] data = o.toByteArray();
        byte[] versionInLFH = Arrays.copyOfRange(data, 4, 6);
        
        assertArrayEquals(new byte[] { 20, 0 }, versionInLFH);
        byte[] gpbInLFH = Arrays.copyOfRange(data, 6, 8);
        
        assertArrayEquals(new byte[] { 0, 8 }, gpbInLFH);

        int cdhStart = findCentralDirectory(data);
        byte[] versionInCDH = Arrays.copyOfRange(data, cdhStart + 6, cdhStart + 8);
        assertArrayEquals(new byte[] { 20, 0 }, versionInCDH);
        byte[] gpbInCDH = Arrays.copyOfRange(data, cdhStart + 8, cdhStart + 10);
        assertArrayEquals(new byte[] { 0, 8 }, gpbInCDH);

        int ddStart = cdhStart - 16;
        assertNotEquals(ZipLong.DD_SIG, new ZipLong(data, ddStart));
        long crcFromLFH = ZipLong.getValue(data, 14);
        long cSizeFromLFH = ZipLong.getValue(data, 18);
        long sizeFromLFH = ZipLong.getValue(data, 22);
        assertEquals(3, sizeFromLFH);

        long crcFromCDH = ZipLong.getValue(data, cdhStart + 16);
        assertEquals(crcFromLFH, crcFromCDH);
        long cSizeFromCDH = ZipLong.getValue(data, cdhStart + 20);
        assertEquals(cSizeFromLFH, cSizeFromCDH);
        long sizeFromCDH = ZipLong.getValue(data, cdhStart + 24);
        assertEquals(sizeFromLFH, sizeFromCDH);
    }

// org.apache.commons.compress.archivers.zip.EncryptedArchiveTest::testReadPasswordEncryptedEntryViaZipFile
    public void testReadPasswordEncryptedEntryViaZipFile()
        throws IOException {
        final File file = getFile("password-encrypted.zip");
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            final ZipArchiveEntry zae = zf.getEntry("LICENSE.txt");
            assertTrue(zae.getGeneralPurposeBit().usesEncryption());
            assertFalse(zae.getGeneralPurposeBit().usesStrongEncryption());
            assertFalse(zf.canReadEntryData(zae));
            try {
                zf.getInputStream(zae);
                fail("expected an exception");
            } catch (final UnsupportedZipFeatureException ex) {
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
        final File file = getFile("password-encrypted.zip");
        ZipArchiveInputStream zin = null;
        try {
            zin = new ZipArchiveInputStream(new FileInputStream(file));
            final ZipArchiveEntry zae = zin.getNextZipEntry();
            assertEquals("LICENSE.txt", zae.getName());
            assertTrue(zae.getGeneralPurposeBit().usesEncryption());
            assertFalse(zae.getGeneralPurposeBit().usesStrongEncryption());
            assertFalse(zin.canReadEntryData(zae));
            try {
                final byte[] buf = new byte[1024];
                zin.read(buf, 0, buf.length);
                fail("expected an exception");
            } catch (final UnsupportedZipFeatureException ex) {
                assertSame(UnsupportedZipFeatureException.Feature.ENCRYPTION,
                           ex.getFeature());
            }
        } finally {
            if (zin != null) {
                zin.close();
            }
        }
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

// org.apache.commons.compress.archivers.zip.Maven221MultiVolumeTest::testRead7ZipMultiVolumeArchiveForStream
    public void testRead7ZipMultiVolumeArchiveForStream() throws IOException {

        final FileInputStream archive =
            new FileInputStream(getFile("apache-maven-2.2.1.zip.001"));
        ZipArchiveInputStream zi = null;
        try {
            zi = new ZipArchiveInputStream(archive,null,false);

            
            
            for (final String element : ENTRIES) {
                assertEquals(element, zi.getNextEntry().getName());
            }

            
            final ArchiveEntry lastEntry = zi.getNextEntry();
            assertEquals(LAST_ENTRY_NAME, lastEntry.getName());
            final byte [] buffer = new byte [4096];

            
            
            
            
            try {
                while (zi.read(buffer) > 0) { }
                fail("shouldn't be able to read from truncated entry");
            } catch (final IOException e) {
                assertEquals("Truncated ZIP file", e.getMessage());
            }

            
            
            try {
                zi.getNextEntry();
                fail("shouldn't be able to read another entry from truncated"
                     + " file");
            } catch (final IOException e) {
                
            }
        } finally {
            if (zi != null) {
                zi.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.Maven221MultiVolumeTest::testRead7ZipMultiVolumeArchiveForFile
    public void testRead7ZipMultiVolumeArchiveForFile() throws IOException {
        final File file = getFile("apache-maven-2.2.1.zip.001");
        ZipFile zf = new ZipFile(file);
        zf.close();
    }

// org.apache.commons.compress.archivers.zip.ParallelScatterZipCreatorTest::concurrent
    public void concurrent()
            throws Exception {
        result = File.createTempFile("parallelScatterGather1", "");
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(result);
        zos.setEncoding("UTF-8");
        final ParallelScatterZipCreator zipCreator = new ParallelScatterZipCreator();

        final Map<String, byte[]> entries = writeEntries(zipCreator);
        zipCreator.writeTo(zos);
        zos.close();
        removeEntriesFoundInZipFile(result, entries);
        assertTrue(entries.size() == 0);
        assertNotNull( zipCreator.getStatisticsMessage());
    }

// org.apache.commons.compress.archivers.zip.ParallelScatterZipCreatorTest::callableApi
    public void callableApi()
            throws Exception {
        result = File.createTempFile("parallelScatterGather2", "");
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(result);
        zos.setEncoding("UTF-8");
        final ExecutorService es = Executors.newFixedThreadPool(1);

        final ScatterGatherBackingStoreSupplier supp = new ScatterGatherBackingStoreSupplier() {
            @Override
            public ScatterGatherBackingStore get() throws IOException {
                return new FileBasedScatterGatherBackingStore(tmp = File.createTempFile("parallelscatter", "n1"));
            }
        };

        final ParallelScatterZipCreator zipCreator = new ParallelScatterZipCreator(es, supp);
        final Map<String, byte[]> entries = writeEntriesAsCallable(zipCreator);
        zipCreator.writeTo(zos);
        zos.close();

        removeEntriesFoundInZipFile(result, entries);
        assertTrue(entries.size() == 0);
        assertNotNull(zipCreator.getStatisticsMessage());
    }

// org.apache.commons.compress.archivers.zip.ScatterSampleTest::testSample
    public void testSample() throws Exception {
        final File result = File.createTempFile("testSample", "fe");

        createFile(result);
        checkFile(result);
    }

// org.apache.commons.compress.archivers.zip.ScatterZipOutputStreamTest::putArchiveEntry
    public void putArchiveEntry() throws Exception {
        scatterFile = File.createTempFile("scattertest", ".notzip");
        final ScatterZipOutputStream scatterZipOutputStream = ScatterZipOutputStream.fileBased(scatterFile);
        final byte[] B_PAYLOAD = "RBBBBBBS".getBytes();
        final byte[] A_PAYLOAD = "XAAY".getBytes();

        final ZipArchiveEntry zab = new ZipArchiveEntry("b.txt");
        zab.setMethod(ZipEntry.DEFLATED);
        final ByteArrayInputStream payload = new ByteArrayInputStream(B_PAYLOAD);
        scatterZipOutputStream.addArchiveEntry(createZipArchiveEntryRequest(zab, createPayloadSupplier(payload)));

        final ZipArchiveEntry zae = new ZipArchiveEntry("a.txt");
        zae.setMethod(ZipEntry.DEFLATED);
        final ByteArrayInputStream payload1 = new ByteArrayInputStream(A_PAYLOAD);
        scatterZipOutputStream.addArchiveEntry(createZipArchiveEntryRequest(zae, createPayloadSupplier(payload1)));

        target = File.createTempFile("scattertest", ".zip");
        final ZipArchiveOutputStream outputStream = new ZipArchiveOutputStream(target);
        scatterZipOutputStream.writeTo( outputStream);
        outputStream.close();
        scatterZipOutputStream.close();

        final ZipFile zf = new ZipFile(target);
        final ZipArchiveEntry b_entry = zf.getEntries("b.txt").iterator().next();
        assertEquals(8, b_entry.getSize());
        assertArrayEquals(B_PAYLOAD, IOUtils.toByteArray(zf.getInputStream(b_entry)));

        final ZipArchiveEntry a_entry = zf.getEntries("a.txt").iterator().next();
        assertEquals(4, a_entry.getSize());
        assertArrayEquals(A_PAYLOAD, IOUtils.toByteArray(zf.getInputStream(a_entry)));
        zf.close();
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
        final File archive = getFile("utf8-7zip-test.zip");
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
        final FileInputStream archive =
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
        final File archive = getFile("utf8-winzip-test.zip");
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
        final FileInputStream archive =
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
        final File file = File.createTempFile("unicode-test", ".zip");
        file.deleteOnExit();
        ZipArchiveInputStream zi = null;
        try {
            createTestFile(file, CharsetNames.US_ASCII, false, true);
            final FileInputStream archive = new FileInputStream(file);
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
        final File file = File.createTempFile("unicode-test", ".zip");
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
        final File archive = getFile("utf8-7zip-test.zip");
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
        final FileInputStream archive =
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
        final File file1 = getFile("utf8-7zip-test.zip");
        final File file2 = getFile("utf8-winzip-test.zip");

        testFile(file1,CP437);
        testFile(file2,CP437);

    }

// org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestampTest::testSampleFile
    public void testSampleFile() {}

// org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestampTest::testMisc
    public void testMisc() throws Exception {
        assertFalse(xf.equals(new Object()));
        assertTrue(xf.toString().startsWith("0x5455 Zip Extra Field"));
        assertTrue(!xf.toString().contains(" Modify:"));
        assertTrue(!xf.toString().contains(" Access:"));
        assertTrue(!xf.toString().contains(" Create:"));
        Object o = xf.clone();
        assertEquals(o.hashCode(), xf.hashCode());
        assertTrue(xf.equals(o));

        xf.setModifyJavaTime(new Date(1111));
        xf.setAccessJavaTime(new Date(2222));
        xf.setCreateJavaTime(new Date(3333));
        xf.setFlags((byte) 7);
        assertFalse(xf.equals(o));
        assertTrue(xf.toString().startsWith("0x5455 Zip Extra Field"));
        assertTrue(xf.toString().contains(" Modify:"));
        assertTrue(xf.toString().contains(" Access:"));
        assertTrue(xf.toString().contains(" Create:"));
        o = xf.clone();
        assertEquals(o.hashCode(), xf.hashCode());
        assertTrue(xf.equals(o));
    }

// org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestampTest::testGettersSetters
    public void testGettersSetters() {
        
        
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2000);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date timeMillis = cal.getTime();
        final ZipLong time = new ZipLong(timeMillis.getTime() / 1000);

        
        try {
            
            xf.setModifyJavaTime(new Date(1000L * (MAX_TIME_SECONDS.getValue() + 1L)));
            fail("Time too big for 32 bits!");
        } catch (final IllegalArgumentException iae) {
            
        }

        
        xf.setModifyTime(time);
        assertEquals(time, xf.getModifyTime());
        assertEquals(timeMillis, xf.getModifyJavaTime());
        xf.setModifyJavaTime(timeMillis);
        assertEquals(time, xf.getModifyTime());
        assertEquals(timeMillis, xf.getModifyJavaTime());
        
        xf.setModifyJavaTime(new Date(timeMillis.getTime() + 123));
        assertEquals(time, xf.getModifyTime());
        assertEquals(timeMillis, xf.getModifyJavaTime());
        
        xf.setModifyTime(null);
        assertNull(xf.getModifyJavaTime());
        xf.setModifyJavaTime(null);
        assertNull(xf.getModifyTime());

        
        xf.setAccessTime(time);
        assertEquals(time, xf.getAccessTime());
        assertEquals(timeMillis, xf.getAccessJavaTime());
        xf.setAccessJavaTime(timeMillis);
        assertEquals(time, xf.getAccessTime());
        assertEquals(timeMillis, xf.getAccessJavaTime());
        
        xf.setAccessJavaTime(new Date(timeMillis.getTime() + 123));
        assertEquals(time, xf.getAccessTime());
        assertEquals(timeMillis, xf.getAccessJavaTime());
        
        xf.setAccessTime(null);
        assertNull(xf.getAccessJavaTime());
        xf.setAccessJavaTime(null);
        assertNull(xf.getAccessTime());

        
        xf.setCreateTime(time);
        assertEquals(time, xf.getCreateTime());
        assertEquals(timeMillis, xf.getCreateJavaTime());
        xf.setCreateJavaTime(timeMillis);
        assertEquals(time, xf.getCreateTime());
        assertEquals(timeMillis, xf.getCreateJavaTime());
        
        xf.setCreateJavaTime(new Date(timeMillis.getTime() + 123));
        assertEquals(time, xf.getCreateTime());
        assertEquals(timeMillis, xf.getCreateJavaTime());
        
        xf.setCreateTime(null);
        assertNull(xf.getCreateJavaTime());
        xf.setCreateJavaTime(null);
        assertNull(xf.getCreateTime());

        
        xf.setModifyTime(time);
        xf.setAccessTime(time);
        xf.setCreateTime(time);

        
        xf.setFlags((byte) 0);
        assertEquals(0, xf.getFlags());
        assertFalse(xf.isBit0_modifyTimePresent());
        assertFalse(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());
        
        assertEquals(1, xf.getLocalFileDataLength().getValue());
        assertEquals(1, xf.getCentralDirectoryLength().getValue());

        
        xf.setFlags((byte) 1);
        assertEquals(1, xf.getFlags());
        assertTrue(xf.isBit0_modifyTimePresent());
        assertFalse(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());
        
        assertEquals(5, xf.getLocalFileDataLength().getValue());
        assertEquals(5, xf.getCentralDirectoryLength().getValue());

        
        xf.setFlags((byte) 2);
        assertEquals(2, xf.getFlags());
        assertFalse(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());
        
        assertEquals(5, xf.getLocalFileDataLength().getValue());
        assertEquals(1, xf.getCentralDirectoryLength().getValue());

        
        xf.setFlags((byte) 4);
        assertEquals(4, xf.getFlags());
        assertFalse(xf.isBit0_modifyTimePresent());
        assertFalse(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        
        assertEquals(5, xf.getLocalFileDataLength().getValue());
        assertEquals(1, xf.getCentralDirectoryLength().getValue());

        
        xf.setFlags((byte) 7);
        assertEquals(7, xf.getFlags());
        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        
        assertEquals(13, xf.getLocalFileDataLength().getValue());
        assertEquals(5, xf.getCentralDirectoryLength().getValue());

        
        xf.setFlags((byte) -1);
        assertEquals(-1, xf.getFlags());
        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        
        assertEquals(13, xf.getLocalFileDataLength().getValue());
        assertEquals(5, xf.getCentralDirectoryLength().getValue());
    }

// org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestampTest::testGetHeaderId
    public void testGetHeaderId() {
        assertEquals(X5455, xf.getHeaderId());
    }

// org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestampTest::testParseReparse
    public void testParseReparse() throws ZipException {
        
        final byte[] NULL_FLAGS = {0};
        final byte[] AC_CENTRAL = {2}; 
        final byte[] CR_CENTRAL = {4}; 

        final byte[] MOD_ZERO = {1, 0, 0, 0, 0};
        final byte[] MOD_MAX = {1, -1, -1, -1, -1};
        final byte[] AC_ZERO = {2, 0, 0, 0, 0};
        final byte[] AC_MAX = {2, -1, -1, -1, -1};
        final byte[] CR_ZERO = {4, 0, 0, 0, 0};
        final byte[] CR_MAX = {4, -1, -1, -1, -1};
        final byte[] MOD_AC_ZERO = {3, 0, 0, 0, 0, 0, 0, 0, 0};
        final byte[] MOD_AC_MAX = {3, -1, -1, -1, -1, -1, -1, -1, -1};
        final byte[] MOD_AC_CR_ZERO = {7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        final byte[] MOD_AC_CR_MAX = {7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

        parseReparse(null, NULL_FLAGS, NULL_FLAGS);
        parseReparse(ZERO_TIME, MOD_ZERO, MOD_ZERO);
        parseReparse(MAX_TIME_SECONDS, MOD_MAX, MOD_MAX);
        parseReparse(ZERO_TIME, AC_ZERO, AC_CENTRAL);
        parseReparse(MAX_TIME_SECONDS, AC_MAX, AC_CENTRAL);
        parseReparse(ZERO_TIME, CR_ZERO, CR_CENTRAL);
        parseReparse(MAX_TIME_SECONDS, CR_MAX, CR_CENTRAL);
        parseReparse(ZERO_TIME, MOD_AC_ZERO, MOD_ZERO);
        parseReparse(MAX_TIME_SECONDS, MOD_AC_MAX, MOD_MAX);
        parseReparse(ZERO_TIME, MOD_AC_CR_ZERO, MOD_ZERO);
        parseReparse(MAX_TIME_SECONDS, MOD_AC_CR_MAX, MOD_MAX);

        
        
        parseReparse((byte) 15, MAX_TIME_SECONDS, (byte) 7, MOD_AC_CR_MAX, MOD_MAX);
        parseReparse((byte) 31, MAX_TIME_SECONDS, (byte) 7, MOD_AC_CR_MAX, MOD_MAX);
        parseReparse((byte) 63, MAX_TIME_SECONDS, (byte) 7, MOD_AC_CR_MAX, MOD_MAX);
        parseReparse((byte) 71, MAX_TIME_SECONDS, (byte) 7, MOD_AC_CR_MAX, MOD_MAX);
        parseReparse((byte) 127, MAX_TIME_SECONDS, (byte) 7, MOD_AC_CR_MAX, MOD_MAX);
        parseReparse((byte) -1, MAX_TIME_SECONDS, (byte) 7, MOD_AC_CR_MAX, MOD_MAX);
    }

// org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestampTest::testWriteReadRoundtrip
    public void testWriteReadRoundtrip() throws IOException {
        tmpDir = mkdir("X5455");
        final File output = new File(tmpDir, "write_rewrite.zip");
        final OutputStream out = new FileOutputStream(output);
        final Date d = new Date(97, 8, 24, 15, 10, 2);
        ZipArchiveOutputStream os = null;
        try {
            os = new ZipArchiveOutputStream(out);
            final ZipArchiveEntry ze = new ZipArchiveEntry("foo");
            xf.setModifyJavaTime(d);
            xf.setFlags((byte) 1);
            ze.addExtraField(xf);
            os.putArchiveEntry(ze);
            os.closeArchiveEntry();
        } finally {
            if (os != null) {
                os.close();
            }
        }
        out.close();
        
        final ZipFile zf = new ZipFile(output);
        final ZipArchiveEntry ze = zf.getEntry("foo");
        final X5455_ExtendedTimestamp ext =
            (X5455_ExtendedTimestamp) ze.getExtraField(X5455);
        assertNotNull(ext);
        assertTrue(ext.isBit0_modifyTimePresent());
        assertEquals(d, ext.getModifyJavaTime());
        zf.close();
    }

// org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestampTest::testBitsAreSetWithTime
    public void testBitsAreSetWithTime() {
        xf.setModifyJavaTime(new Date(1111));
        assertTrue(xf.isBit0_modifyTimePresent());
        assertEquals(1, xf.getFlags());
        xf.setAccessJavaTime(new Date(2222));
        assertTrue(xf.isBit1_accessTimePresent());
        assertEquals(3, xf.getFlags());
        xf.setCreateJavaTime(new Date(3333));
        assertTrue(xf.isBit2_createTimePresent());
        assertEquals(7, xf.getFlags());
        xf.setModifyJavaTime(null);
        assertFalse(xf.isBit0_modifyTimePresent());
        assertEquals(6, xf.getFlags());
        xf.setAccessJavaTime(null);
        assertFalse(xf.isBit1_accessTimePresent());
        assertEquals(4, xf.getFlags());
        xf.setCreateJavaTime(null);
        assertFalse(xf.isBit2_createTimePresent());
        assertEquals(0, xf.getFlags());
    }

// org.apache.commons.compress.archivers.zip.X7875_NewUnixTest::testSampleFile
    public void testSampleFile() throws Exception {
        final File archive = getFile("COMPRESS-211_uid_gid_zip_test.zip");
        ZipFile zf = null;

        try {
            zf = new ZipFile(archive);
            final Enumeration<ZipArchiveEntry> en = zf.getEntries();

            
            
            while (en.hasMoreElements()) {

                final ZipArchiveEntry zae = en.nextElement();
                final String name = zae.getName();
                final X7875_NewUnix xf = (X7875_NewUnix) zae.getExtraField(X7875);

                
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
        final Object o = xf.clone();
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

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testExtraFields
    public void testExtraFields() {
        final AsiExtraField a = new AsiExtraField();
        a.setDirectory(true);
        a.setMode(0755);
        final UnrecognizedExtraField u = new UnrecognizedExtraField();
        u.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u.setLocalFileDataData(new byte[0]);

        final ZipArchiveEntry ze = new ZipArchiveEntry("test/");
        ze.setExtraFields(new ZipExtraField[] {a, u});
        final byte[] data1 = ze.getExtra();
        ZipExtraField[] result = ze.getExtraFields();
        assertEquals("first pass", 2, result.length);
        assertSame(a, result[0]);
        assertSame(u, result[1]);

        final UnrecognizedExtraField u2 = new UnrecognizedExtraField();
        u2.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u2.setLocalFileDataData(new byte[] {1});

        ze.addExtraField(u2);
        final byte[] data2 = ze.getExtra();
        result = ze.getExtraFields();
        assertEquals("second pass", 2, result.length);
        assertSame(a, result[0]);
        assertSame(u2, result[1]);
        assertEquals("length second pass", data1.length+1, data2.length);

        final UnrecognizedExtraField u3 = new UnrecognizedExtraField();
        u3.setHeaderId(new ZipShort(2));
        u3.setLocalFileDataData(new byte[] {1});
        ze.addExtraField(u3);
        result = ze.getExtraFields();
        assertEquals("third pass", 3, result.length);

        ze.removeExtraField(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        final byte[] data3 = ze.getExtra();
        result = ze.getExtraFields();
        assertEquals("fourth pass", 2, result.length);
        assertSame(a, result[0]);
        assertSame(u3, result[1]);
        assertEquals("length fourth pass", data2.length, data3.length);

        try {
            ze.removeExtraField(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
            fail("should be no such element");
        } catch (final java.util.NoSuchElementException nse) {
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testExtraFieldMerging
    public void testExtraFieldMerging() {
        final AsiExtraField a = new AsiExtraField();
        a.setDirectory(true);
        a.setMode(0755);
        final UnrecognizedExtraField u = new UnrecognizedExtraField();
        u.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u.setLocalFileDataData(new byte[0]);

        final ZipArchiveEntry ze = new ZipArchiveEntry("test/");
        ze.setExtraFields(new ZipExtraField[] {a, u});

        
        
        final byte[] b = ExtraFieldUtilsTest.UNRECOGNIZED_HEADER.getBytes();
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
        final AsiExtraField a = new AsiExtraField();
        a.setDirectory(true);
        a.setMode(0755);
        final UnrecognizedExtraField u = new UnrecognizedExtraField();
        u.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u.setLocalFileDataData(new byte[0]);

        final ZipArchiveEntry ze = new ZipArchiveEntry("test/");
        ze.setExtraFields(new ZipExtraField[] {a, u});
        final byte[] data1 = ze.getExtra();

        final UnrecognizedExtraField u2 = new UnrecognizedExtraField();
        u2.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
        u2.setLocalFileDataData(new byte[] {1});

        ze.addAsFirstExtraField(u2);
        final byte[] data2 = ze.getExtra();
        ZipExtraField[] result = ze.getExtraFields();
        assertEquals("second pass", 2, result.length);
        assertSame(u2, result[0]);
        assertSame(a, result[1]);
        assertEquals("length second pass", data1.length + 1, data2.length);

        final UnrecognizedExtraField u3 = new UnrecognizedExtraField();
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
        final ZipArchiveOutputStream zos =
            new ZipArchiveOutputStream(new ByteArrayOutputStream());
        final ZipArchiveEntry entry = new ZipArchiveEntry("foo");
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
        final ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        final ZipArchiveEntry entry2 = new ZipArchiveEntry("bar");
        assertFalse(entry1.equals(entry2));
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testNullCommentEqualsEmptyComment
    public void testNullCommentEqualsEmptyComment() {
        final ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        final ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        final ZipArchiveEntry entry3 = new ZipArchiveEntry("foo");
        entry1.setComment(null);
        entry2.setComment("");
        entry3.setComment("bar");
        assertEquals(entry1, entry2);
        assertFalse(entry1.equals(entry3));
        assertFalse(entry2.equals(entry3));
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testCopyConstructor
    public void testCopyConstructor() throws Exception {
        final ZipArchiveEntry archiveEntry = new ZipArchiveEntry("fred");
        archiveEntry.setUnixMode(0664);
        archiveEntry.setMethod(ZipEntry.DEFLATED);
        archiveEntry.getGeneralPurposeBit().useStrongEncryption(true);
        final ZipArchiveEntry copy = new ZipArchiveEntry(archiveEntry);
        assertEquals(archiveEntry, copy);
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet
    public void isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet() throws Exception {
        try (ZipFile zf = new ZipFile(getFile("COMPRESS-379.jar"))) {
            ZipArchiveEntry ze = zf.getEntry("META-INF/maven/");
            assertFalse(ze.isUnixSymlink());
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::testIsUnixSymlink
    public void testIsUnixSymlink() {
        ZipArchiveEntry ze = new ZipArchiveEntry("foo");
        ze.setUnixMode(UnixStat.LINK_FLAG);
        assertTrue(ze.isUnixSymlink());
        ze.setUnixMode(UnixStat.LINK_FLAG | UnixStat.DIR_FLAG);
        assertFalse(ze.isUnixSymlink());
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

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::testWithBytesAfterData
    public void testWithBytesAfterData() throws Exception {
        final int expectedNumEntries = 2;
        final InputStream is = ZipArchiveInputStreamTest.class
                .getResourceAsStream("/archive_with_bytes_after_data.zip");
        final ZipArchiveInputStream zip = new ZipArchiveInputStream(is);

        try {
            int actualNumEntries = 0;
            ZipArchiveEntry zae = zip.getNextZipEntry();
            while (zae != null) {
                actualNumEntries++;
                readEntry(zip, zae);
                zae = zip.getNextZipEntry();
            }
            assertEquals(expectedNumEntries, actualNumEntries);
        } finally {
            zip.close();
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::testThrowOnInvalidEntry
    public void testThrowOnInvalidEntry() throws Exception {
        final InputStream is = ZipArchiveInputStreamTest.class
                .getResourceAsStream("/invalid-zip.zip");
        final ZipArchiveInputStream zip = new ZipArchiveInputStream(is);

        try {
            zip.getNextZipEntry();
            fail("IOException expected");
        } catch (ZipException expected) {
            assertTrue(expected.getMessage().contains("Unexpected record signature"));
        } finally {
            zip.close();
        }
    }

// org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::testOffsets
    public void testOffsets() throws Exception {
        
        try (InputStream archiveStream = ZipArchiveInputStream.class.getResourceAsStream("/mixed.zip");
             ZipArchiveInputStream zipStream =  new ZipArchiveInputStream((archiveStream))
        ) {
            ZipArchiveEntry inflatedEntry = zipStream.getNextZipEntry();
            Assert.assertEquals("inflated.txt", inflatedEntry.getName());
            Assert.assertEquals(0x0000, inflatedEntry.getLocalHeaderOffset());
            Assert.assertEquals(0x0046, inflatedEntry.getDataOffset());
            ZipArchiveEntry storedEntry = zipStream.getNextZipEntry();
            Assert.assertEquals("stored.txt", storedEntry.getName());
            Assert.assertEquals(0x5892, storedEntry.getLocalHeaderOffset());
            Assert.assertEquals(0x58d6, storedEntry.getDataOffset());
            Assert.assertNull(zipStream.getNextZipEntry());
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

// org.apache.commons.compress.archivers.zip.ZipFileTest::testCDOrderInMemory
    public void testCDOrderInMemory() throws Exception {
        byte[] data = null;
        try (FileInputStream fis = new FileInputStream(getFile("ordertest.zip"))) {
            data = IOUtils.toByteArray(fis);
        }

        zf = new ZipFile(new SeekableInMemoryByteChannel(data), ZipEncodingHelper.UTF8);
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

// org.apache.commons.compress.archivers.zip.ZipFileTest::testConcurrentReadSeekable
    public void testConcurrentReadSeekable() throws Exception {
        
        byte[] data = null;
        try (FileInputStream fis = new FileInputStream(getFile("mixed.zip"))) {
            data = IOUtils.toByteArray(fis);
        }
        zf = new ZipFile(new SeekableInMemoryByteChannel(data), ZipEncodingHelper.UTF8);

        final Map<String, byte[]> content = new HashMap<String, byte[]>();
        for (ZipArchiveEntry entry: Collections.list(zf.getEntries())) {
            content.put(entry.getName(), IOUtils.toByteArray(zf.getInputStream(entry)));
        }

        final AtomicInteger passedCount = new AtomicInteger();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (ZipArchiveEntry entry: Collections.list(zf.getEntries())) {
                    assertAllReadMethods(content.get(entry.getName()), zf, entry);
                }
                passedCount.incrementAndGet();
            }
        };
        Thread t0 = new Thread(run);
        Thread t1 = new Thread(run);
        t0.start();
        t1.start();
        t0.join();
        t1.join();
        assertEquals(2, passedCount.get());
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testConcurrentReadFile
    public void testConcurrentReadFile() throws Exception {
        
        final File archive = getFile("mixed.zip");
        zf = new ZipFile(archive);

        final Map<String, byte[]> content = new HashMap<String, byte[]>();
        for (ZipArchiveEntry entry: Collections.list(zf.getEntries())) {
            content.put(entry.getName(), IOUtils.toByteArray(zf.getInputStream(entry)));
        }

        final AtomicInteger passedCount = new AtomicInteger();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (ZipArchiveEntry entry: Collections.list(zf.getEntries())) {
                    assertAllReadMethods(content.get(entry.getName()), zf, entry);
                }
                passedCount.incrementAndGet();
            }
        };
        Thread t0 = new Thread(run);
        Thread t1 = new Thread(run);
        t0.start();
        t1.start();
        t0.join();
        t1.join();
        assertEquals(2, passedCount.get());
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testOffsets
    public void testOffsets() throws Exception {
        
        final File archive = getFile("mixed.zip");
        try (ZipFile zf = new ZipFile(archive)) {
            ZipArchiveEntry inflatedEntry = zf.getEntry("inflated.txt");
            Assert.assertEquals(0x0000, inflatedEntry.getLocalHeaderOffset());
            Assert.assertEquals(0x0046, inflatedEntry.getDataOffset());
            Assert.assertTrue(inflatedEntry.isStreamContiguous());
            ZipArchiveEntry storedEntry = zf.getEntry("stored.txt");
            Assert.assertEquals(0x5892, storedEntry.getLocalHeaderOffset());
            Assert.assertEquals(0x58d6, storedEntry.getDataOffset());
            Assert.assertTrue(inflatedEntry.isStreamContiguous());
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testDelayedOffsetsAndSizes
    public void testDelayedOffsetsAndSizes() throws Exception {
        ByteArrayOutputStream zipContent = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(zipContent)) {
            ZipArchiveEntry inflatedEntry = new ZipArchiveEntry("inflated.txt");
            inflatedEntry.setMethod(ZipEntry.DEFLATED);
            zipOutput.putArchiveEntry(inflatedEntry);
            zipOutput.write("Hello Deflated\n".getBytes());
            zipOutput.closeArchiveEntry();

            byte[] storedContent = "Hello Stored\n".getBytes();
            ZipArchiveEntry storedEntry = new ZipArchiveEntry("stored.txt");
            storedEntry.setMethod(ZipEntry.STORED);
            storedEntry.setSize(storedContent.length);
            storedEntry.setCrc(calculateCrc32(storedContent));
            zipOutput.putArchiveEntry(storedEntry);
            zipOutput.write("Hello Stored\n".getBytes());
            zipOutput.closeArchiveEntry();

        }

        try (ZipFile zf = new ZipFile(new SeekableInMemoryByteChannel(zipContent.toByteArray()))) {
            ZipArchiveEntry inflatedEntry = zf.getEntry("inflated.txt");
            Assert.assertNotEquals(-1L, inflatedEntry.getLocalHeaderOffset());
            Assert.assertNotEquals(-1L, inflatedEntry.getDataOffset());
            Assert.assertTrue(inflatedEntry.isStreamContiguous());
            Assert.assertNotEquals(-1L, inflatedEntry.getCompressedSize());
            Assert.assertNotEquals(-1L, inflatedEntry.getSize());
            ZipArchiveEntry storedEntry = zf.getEntry("stored.txt");
            Assert.assertNotEquals(-1L, storedEntry.getLocalHeaderOffset());
            Assert.assertNotEquals(-1L, storedEntry.getDataOffset());
            Assert.assertTrue(inflatedEntry.isStreamContiguous());
            Assert.assertNotEquals(-1L, storedEntry.getCompressedSize());
            Assert.assertNotEquals(-1L, storedEntry.getSize());
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testEntryAlignment
    public void testEntryAlignment() throws Exception {
        SeekableInMemoryByteChannel zipContent = new SeekableInMemoryByteChannel();
        try (ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(zipContent)) {
            ZipArchiveEntry inflatedEntry = new ZipArchiveEntry("inflated.txt");
            inflatedEntry.setMethod(ZipEntry.DEFLATED);
            inflatedEntry.setAlignment(1024);
            zipOutput.putArchiveEntry(inflatedEntry);
            zipOutput.write("Hello Deflated\n".getBytes(Charset.forName("UTF-8")));
            zipOutput.closeArchiveEntry();

            ZipArchiveEntry storedEntry = new ZipArchiveEntry("stored.txt");
            storedEntry.setMethod(ZipEntry.STORED);
            storedEntry.setAlignment(1024);
            zipOutput.putArchiveEntry(storedEntry);
            zipOutput.write("Hello Stored\n".getBytes(Charset.forName("UTF-8")));
            zipOutput.closeArchiveEntry();

            ZipArchiveEntry storedEntry2 = new ZipArchiveEntry("stored2.txt");
            storedEntry2.setMethod(ZipEntry.STORED);
            storedEntry2.setAlignment(1024);
            storedEntry2.addExtraField(new ResourceAlignmentExtraField(1));
            zipOutput.putArchiveEntry(storedEntry2);
            zipOutput.write("Hello overload-alignment Stored\n".getBytes(Charset.forName("UTF-8")));
            zipOutput.closeArchiveEntry();

            ZipArchiveEntry storedEntry3 = new ZipArchiveEntry("stored3.txt");
            storedEntry3.setMethod(ZipEntry.STORED);
            storedEntry3.addExtraField(new ResourceAlignmentExtraField(1024));
            zipOutput.putArchiveEntry(storedEntry3);
            zipOutput.write("Hello copy-alignment Stored\n".getBytes(Charset.forName("UTF-8")));
            zipOutput.closeArchiveEntry();

        }

        try (ZipFile zf = new ZipFile(new SeekableInMemoryByteChannel(
                        Arrays.copyOfRange(zipContent.array(), 0, (int)zipContent.size())
        ))) {
            ZipArchiveEntry inflatedEntry = zf.getEntry("inflated.txt");
            ResourceAlignmentExtraField inflatedAlignmentEx =
                            (ResourceAlignmentExtraField)inflatedEntry.getExtraField(ResourceAlignmentExtraField.ID);
            assertNotEquals(-1L, inflatedEntry.getCompressedSize());
            assertNotEquals(-1L, inflatedEntry.getSize());
            assertEquals(0L, inflatedEntry.getDataOffset()%1024);
            assertNotNull(inflatedAlignmentEx);
            assertEquals(1024, inflatedAlignmentEx.getAlignment());
            assertFalse(inflatedAlignmentEx.allowMethodChange());
            try (InputStream stream = zf.getInputStream(inflatedEntry)) {
                Assert.assertEquals("Hello Deflated\n",
                                new String(IOUtils.toByteArray(stream), Charset.forName("UTF-8")));
            }
            ZipArchiveEntry storedEntry = zf.getEntry("stored.txt");
            ResourceAlignmentExtraField storedAlignmentEx =
                            (ResourceAlignmentExtraField)storedEntry.getExtraField(ResourceAlignmentExtraField.ID);
            assertNotEquals(-1L, storedEntry.getCompressedSize());
            assertNotEquals(-1L, storedEntry.getSize());
            assertEquals(0L, storedEntry.getDataOffset()%1024);
            assertNotNull(storedAlignmentEx);
            assertEquals(1024, storedAlignmentEx.getAlignment());
            assertFalse(storedAlignmentEx.allowMethodChange());
            try (InputStream stream = zf.getInputStream(storedEntry)) {
                Assert.assertEquals("Hello Stored\n",
                                new String(IOUtils.toByteArray(stream), Charset.forName("UTF-8")));
            }

            ZipArchiveEntry storedEntry2 = zf.getEntry("stored2.txt");
            ResourceAlignmentExtraField stored2AlignmentEx =
                            (ResourceAlignmentExtraField)storedEntry2.getExtraField(ResourceAlignmentExtraField.ID);
            assertNotEquals(-1L, storedEntry2.getCompressedSize());
            assertNotEquals(-1L, storedEntry2.getSize());
            assertEquals(0L, storedEntry2.getDataOffset()%1024);
            assertNotNull(stored2AlignmentEx);
            assertEquals(1024, stored2AlignmentEx.getAlignment());
            assertFalse(stored2AlignmentEx.allowMethodChange());
            try (InputStream stream = zf.getInputStream(storedEntry2)) {
                Assert.assertEquals("Hello overload-alignment Stored\n",
                                new String(IOUtils.toByteArray(stream), Charset.forName("UTF-8")));
            }

            ZipArchiveEntry storedEntry3 = zf.getEntry("stored3.txt");
            ResourceAlignmentExtraField stored3AlignmentEx =
                            (ResourceAlignmentExtraField)storedEntry3.getExtraField(ResourceAlignmentExtraField.ID);
            assertNotEquals(-1L, storedEntry3.getCompressedSize());
            assertNotEquals(-1L, storedEntry3.getSize());
            assertEquals(0L, storedEntry3.getDataOffset()%1024);
            assertNotNull(stored3AlignmentEx);
            assertEquals(1024, stored3AlignmentEx.getAlignment());
            assertFalse(stored3AlignmentEx.allowMethodChange());
            try (InputStream stream = zf.getInputStream(storedEntry3)) {
                Assert.assertEquals("Hello copy-alignment Stored\n",
                                new String(IOUtils.toByteArray(stream), Charset.forName("UTF-8")));
            }
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testEntryAlignmentExceed
    public void testEntryAlignmentExceed() throws Exception {
        SeekableInMemoryByteChannel zipContent = new SeekableInMemoryByteChannel();
        try (ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(zipContent)) {
            ZipArchiveEntry inflatedEntry = new ZipArchiveEntry("inflated.txt");
            inflatedEntry.setMethod(ZipEntry.STORED);
            inflatedEntry.setAlignment(0x20000);
        }
    }

// org.apache.commons.compress.archivers.zip.ZipFileTest::testInvalidAlignment
    public void testInvalidAlignment() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("dummy");
        entry.setAlignment(3);
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

        final List<String> expected = new ArrayList<>();
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
        final List<String> expected = new ArrayList<>();
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
        final List<String> expected = new ArrayList<>();
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
        final List<String> expected = new ArrayList<>();
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
        final List<String> expected = new ArrayList<>();
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

        final List<String> expected = new ArrayList<>();
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
        final List<String> expected = new ArrayList<>();
        expected.add("test1.xml");
        expected.add("test.txt");
        this.checkArchiveContent(temp, expected);
    }
