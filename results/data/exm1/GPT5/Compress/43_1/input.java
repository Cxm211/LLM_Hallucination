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

// org.apache.commons.compress.compressors.BZip2TestCase::testBzipCreation
    public void testBzipCreation()  throws Exception {
        File output = null;
        final File input = getFile("test.txt");
        {
            output = new File(dir, "test.txt.bz2");
            final OutputStream out = new FileOutputStream(output);
            final CompressorOutputStream cos = new CompressorStreamFactory().createCompressorOutputStream("bzip2", out);
            final FileInputStream in = new FileInputStream(input);
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
            final FileOutputStream os = new FileOutputStream(decompressed);
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
        final FileOutputStream os = new FileOutputStream(output);
        IOUtils.copy(in, os);
        is.close();
        os.close();
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testConcatenatedStreamsReadFirstOnly
    public void testConcatenatedStreamsReadFirstOnly() throws Exception {
        final File input = getFile("multiple.bz2");
        try (InputStream is = new FileInputStream(input)) {
            try (CompressorInputStream in = new CompressorStreamFactory()
                    .createCompressorInputStream("bzip2", is)) {
                assertEquals('a', in.read());
                assertEquals(-1, in.read());
            }
        }
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testConcatenatedStreamsReadFully
    public void testConcatenatedStreamsReadFully() throws Exception {
        final File input = getFile("multiple.bz2");
        try (InputStream is = new FileInputStream(input)) {
            try (CompressorInputStream in = new BZip2CompressorInputStream(is, true)) {
                assertEquals('a', in.read());
                assertEquals('b', in.read());
                assertEquals(0, in.available());
                assertEquals(-1, in.read());
            }
        }
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testCOMPRESS131
    public void testCOMPRESS131() throws Exception {
        final File input = getFile("COMPRESS-131.bz2");
        try (InputStream is = new FileInputStream(input)) {
            try (CompressorInputStream in = new BZip2CompressorInputStream(is, true)) {
                int l = 0;
                while (in.read() != -1) {
                    l++;
                }
                assertEquals(539, l);
            }
        }
    }

// org.apache.commons.compress.compressors.DeflateTestCase::testDeflateCreation
    public void testDeflateCreation()  throws Exception {
        final File input = getFile("test1.xml");
        final File output = new File(dir, "test1.xml.deflatez");
        try (OutputStream out = new FileOutputStream(output)) {
            try (CompressorOutputStream cos = new CompressorStreamFactory()
                    .createCompressorOutputStream("deflate", out)) {
                IOUtils.copy(new FileInputStream(input), cos);
            }
        }
    }

// org.apache.commons.compress.compressors.DeflateTestCase::testRawDeflateCreation
    public void testRawDeflateCreation()  throws Exception {
        final File input = getFile("test1.xml");
        final File output = new File(dir, "test1.xml.deflate");
        try (OutputStream out = new FileOutputStream(output)) {
            final DeflateParameters params = new DeflateParameters();
            params.setWithZlibHeader(false);
            try (CompressorOutputStream cos = new DeflateCompressorOutputStream(out, params)) {
                IOUtils.copy(new FileInputStream(input), cos);
            }
        }
    }

// org.apache.commons.compress.compressors.DeflateTestCase::testDeflateUnarchive
    public void testDeflateUnarchive() throws Exception {
        final File input = getFile("bla.tar.deflatez");
        final File output = new File(dir, "bla.tar");
        try (InputStream is = new FileInputStream(input)) {
            final CompressorInputStream in = new CompressorStreamFactory()
                    .createCompressorInputStream("deflate", is); 
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
        }
    }

// org.apache.commons.compress.compressors.DeflateTestCase::testRawDeflateUnarchive
    public void testRawDeflateUnarchive() throws Exception {
        final File input = getFile("bla.tar.deflate");
        final File output = new File(dir, "bla.tar");
        try (InputStream is = new FileInputStream(input)) {
            final DeflateParameters params = new DeflateParameters();
            params.setWithZlibHeader(false);
            final CompressorInputStream in = new DeflateCompressorInputStream(is, params);
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
        }
    }

// org.apache.commons.compress.compressors.FramedSnappyTestCase::testDefaultExtraction
    public void testDefaultExtraction() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            @Override
            public CompressorInputStream wrap(final InputStream is) throws IOException {
                return new FramedSnappyCompressorInputStream(is);
            }
        });
    }

// org.apache.commons.compress.compressors.FramedSnappyTestCase::testDefaultExtractionViaFactory
    public void testDefaultExtractionViaFactory() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            @Override
            public CompressorInputStream wrap(final InputStream is) throws Exception {
                return new CompressorStreamFactory()
                    .createCompressorInputStream(CompressorStreamFactory.SNAPPY_FRAMED,
                                                 is);
            }
        });
    }

// org.apache.commons.compress.compressors.FramedSnappyTestCase::testDefaultExtractionViaFactoryAutodetection
    public void testDefaultExtractionViaFactoryAutodetection() throws Exception {
        testUnarchive(new StreamWrapper<CompressorInputStream>() {
            @Override
            public CompressorInputStream wrap(final InputStream is) throws Exception {
                return new CompressorStreamFactory().createCompressorInputStream(is);
            }
        });
    }

// org.apache.commons.compress.compressors.FramedSnappyTestCase::testRoundtrip
    public void testRoundtrip() throws Exception {
        testRoundtrip(getFile("test.txt"));
        testRoundtrip(getFile("bla.tar"));
        testRoundtrip(getFile("COMPRESS-256.7z"));
    }

// org.apache.commons.compress.compressors.FramedSnappyTestCase::testRoundtripWithOneBigWrite
    public void testRoundtripWithOneBigWrite() throws Exception {
        Random r = new Random();
        File input = new File(dir, "bigChunkTest");
        try (FileOutputStream fs = new FileOutputStream(input)) {
            for (int i = 0 ; i < 1 << 17; i++) {
                fs.write(r.nextInt(256));
            }
        }
        long start = System.currentTimeMillis();
        final File outputSz = new File(dir, input.getName() + ".sz");
        try (FileInputStream is = new FileInputStream(input);
             FileOutputStream os = new FileOutputStream(outputSz);
             CompressorOutputStream sos = new CompressorStreamFactory()
                 .createCompressorOutputStream("snappy-framed", os)) {
            byte[] b = IOUtils.toByteArray(is);
            sos.write(b[0]);
            sos.write(b, 1, b.length - 1); 
        }
        System.err.println(input.getName() + " written, uncompressed bytes: " + input.length()
            + ", compressed bytes: " + outputSz.length() + " after " + (System.currentTimeMillis() - start) + "ms");
        try (FileInputStream is = new FileInputStream(input);
             CompressorInputStream sis = new CompressorStreamFactory()
                 .createCompressorInputStream("snappy-framed", new FileInputStream(outputSz))) {
            byte[] expected = IOUtils.toByteArray(is);
            byte[] actual = IOUtils.toByteArray(sis);
            assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testGzipCreation
    public void testGzipCreation()  throws Exception {
        final File input = getFile("test1.xml");
        final File output = new File(dir, "test1.xml.gz");
        try (OutputStream out = new FileOutputStream(output)) {
            try (CompressorOutputStream cos = new CompressorStreamFactory()
                    .createCompressorOutputStream("gz", out)) {
                IOUtils.copy(new FileInputStream(input), cos);
            }
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testGzipUnarchive
    public void testGzipUnarchive() throws Exception {
        final File input = getFile("bla.tgz");
        final File output = new File(dir, "bla.tar");
        try (InputStream is = new FileInputStream(input)) {
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
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testConcatenatedStreamsReadFirstOnly
    public void testConcatenatedStreamsReadFirstOnly() throws Exception {
        final File input = getFile("multiple.gz");
        try (InputStream is = new FileInputStream(input)) {
            try (CompressorInputStream in = new CompressorStreamFactory()
                    .createCompressorInputStream("gz", is)) {
                assertEquals('a', in.read());
                assertEquals(-1, in.read());
            }
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testConcatenatedStreamsReadFully
    public void testConcatenatedStreamsReadFully() throws Exception {
        final File input = getFile("multiple.gz");
        try (InputStream is = new FileInputStream(input)) {
            try (CompressorInputStream in = new GzipCompressorInputStream(is, true)) {
                assertEquals('a', in.read());
                assertEquals('b', in.read());
                assertEquals(0, in.available());
                assertEquals(-1, in.read());
            }
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

            final byte[] data = ((ByteArrayOutputStream) out).toByteArray();
            in = new ByteArrayInputStream(data, 0, data.length - 1);
            cin = new CompressorStreamFactory()
                .createCompressorInputStream("gz", in);
            out = new ByteArrayOutputStream();

            try {
                IOUtils.copy(cin, out);
                fail("Expected an exception");
            } catch (final IOException ioex) {
                
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
        byte[] content;
        try (FileInputStream fis = new FileInputStream(getFile("test3.xml"))) {
            content = IOUtils.toByteArray(fis);
        }
        
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();

        final GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
        parameters.setOperatingSystem(3);
        parameters.setFilename("test3.xml");
        parameters.setComment("Test file");
        parameters.setModificationTime(System.currentTimeMillis());
        final GzipCompressorOutputStream out = new GzipCompressorOutputStream(bout, parameters);
        out.write(content);
        out.flush();
        out.close();

        final GzipCompressorInputStream in = new GzipCompressorInputStream(new ByteArrayInputStream(bout.toByteArray()));
        final byte[] content2 = IOUtils.toByteArray(in);

        Assert.assertArrayEquals("uncompressed content", content, content2);
    }

// org.apache.commons.compress.compressors.GZipTestCase::testInteroperabilityWithGZIPInputStream
    public void testInteroperabilityWithGZIPInputStream() throws Exception {
        byte[] content;
        try (FileInputStream fis = new FileInputStream(getFile("test3.xml"))) {
            content = IOUtils.toByteArray(fis);
        }
        
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();

        final GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
        parameters.setOperatingSystem(3);
        parameters.setFilename("test3.xml");
        parameters.setComment("Test file");
        parameters.setModificationTime(System.currentTimeMillis());
        final GzipCompressorOutputStream out = new GzipCompressorOutputStream(bout, parameters);
        out.write(content);
        out.flush();
        out.close();

        final GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        final byte[] content2 = IOUtils.toByteArray(in);

        Assert.assertArrayEquals("uncompressed content", content, content2);
    }

// org.apache.commons.compress.compressors.GZipTestCase::testInvalidCompressionLevel
    public void testInvalidCompressionLevel() {
        final GzipParameters parameters = new GzipParameters();
        try {
            parameters.setCompressionLevel(10);
            fail("IllegalArgumentException not thrown");
        } catch (final IllegalArgumentException e) {
            
        }
        
        try {
            parameters.setCompressionLevel(-5);
            fail("IllegalArgumentException not thrown");
        } catch (final IllegalArgumentException e) {
            
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
        final GzipCompressorOutputStream out = new GzipCompressorOutputStream(new ByteArrayOutputStream());
        out.close();
        try {
            out.write(0);
            fail("IOException expected");
        } catch (final IOException e) {
            
        }
    }

// org.apache.commons.compress.compressors.GZipTestCase::testMetadataRoundTrip
    public void testMetadataRoundTrip() throws Exception {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                
        final GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
        parameters.setModificationTime(123456000);
        parameters.setOperatingSystem(13);
        parameters.setFilename("test3.xml");
        parameters.setComment("Umlaute möglich?");
        try (GzipCompressorOutputStream out = new GzipCompressorOutputStream(bout, parameters); FileInputStream fis = new FileInputStream(getFile("test3" +
                ".xml"))) {
            IOUtils.copy(fis, out);
        }
        
        final GzipCompressorInputStream input =
            new GzipCompressorInputStream(new ByteArrayInputStream(bout.toByteArray()));
        input.close();
        final GzipParameters readParams = input.getMetaData();
        assertEquals(Deflater.BEST_COMPRESSION, readParams.getCompressionLevel());
        assertEquals(123456000, readParams.getModificationTime());
        assertEquals(13, readParams.getOperatingSystem());
        assertEquals("test3.xml", readParams.getFilename());
        assertEquals("Umlaute möglich?", readParams.getComment());
    }

// org.apache.commons.compress.compressors.LZMATestCase::lzmaRoundtrip
    public void lzmaRoundtrip() throws Exception {
        final File input = getFile("test1.xml");
        final File compressed = new File(dir, "test1.xml.xz");
        try (OutputStream out = new FileOutputStream(compressed)) {
            try (CompressorOutputStream cos = new CompressorStreamFactory()
                    .createCompressorOutputStream("lzma", out)) {
                IOUtils.copy(new FileInputStream(input), cos);
            }
        }
        byte[] orig;
        try (InputStream is = new FileInputStream(input)) {
            orig = IOUtils.toByteArray(is);
        }
        byte[] uncompressed;
        try (InputStream is = new FileInputStream(compressed);
             CompressorInputStream in = new LZMACompressorInputStream(is)) {
            uncompressed = IOUtils.toByteArray(in);
        }
        Assert.assertArrayEquals(orig, uncompressed);
    }

// org.apache.commons.compress.compressors.LZMATestCase::testLZMAUnarchive
    public void testLZMAUnarchive() throws Exception {
        final File input = getFile("bla.tar.lzma");
        final File output = new File(dir, "bla.tar");
        try (InputStream is = new FileInputStream(input)) {
            final CompressorInputStream in = new LZMACompressorInputStream(is);
            copy(in, output);
        }
    }

// org.apache.commons.compress.compressors.LZMATestCase::testLZMAUnarchiveWithAutodetection
    public void testLZMAUnarchiveWithAutodetection() throws Exception {
        final File input = getFile("bla.tar.lzma");
        final File output = new File(dir, "bla.tar");
        try (InputStream is = new BufferedInputStream(new FileInputStream(input))) {
            final CompressorInputStream in = new CompressorStreamFactory()
                    .createCompressorInputStream(is);
            copy(in, output);
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
        try (InputStream is = new FileInputStream(getFile("bla.pack"))) {
            final byte[] sig = new byte[4];
            is.read(sig);
            assertTrue(Pack200CompressorInputStream.matches(sig, 4));
        }
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testBadSignature
    public void testBadSignature() throws Exception {
        try (InputStream is = new FileInputStream(getFile("bla.jar"))) {
            final byte[] sig = new byte[4];
            is.read(sig);
            assertFalse(Pack200CompressorInputStream.matches(sig, 4));
        }
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testShortSignature
    public void testShortSignature() throws Exception {
        try (InputStream is = new FileInputStream(getFile("bla.pack"))) {
            final byte[] sig = new byte[2];
            is.read(sig);
            assertFalse(Pack200CompressorInputStream.matches(sig, 2));
        }
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testInputStreamMethods
    public void testInputStreamMethods() throws Exception {
        final Map<String, String> m = new HashMap<>();
        m.put("foo", "bar");
        try (InputStream is = new Pack200CompressorInputStream(new FileInputStream(getFile("bla.jar")),
                m)) {
            
            
            assertTrue(is.markSupported());
            is.mark(5);
            assertEquals(0x50, is.read());
            final byte[] rest = new byte[3];
            assertEquals(3, is.read(rest));
            assertEquals(0x4b, rest[0]);
            assertEquals(3, rest[1]);
            assertEquals(4, rest[2]);
            assertEquals(1, is.skip(1));
            is.reset();
            assertEquals(0x50, is.read());
            assertTrue(is.available() > 0);
        }
    }

// org.apache.commons.compress.compressors.Pack200TestCase::testOutputStreamMethods
    public void testOutputStreamMethods() throws Exception {
        final File output = new File(dir, "bla.pack");
        final Map<String, String> m = new HashMap<>();
        m.put("foo", "bar");
        try (OutputStream out = new FileOutputStream(output)) {
            final OutputStream os = new Pack200CompressorOutputStream(out, m);
            os.write(1);
            os.write(new byte[] { 2, 3 });
            os.close();
        }
    }

// org.apache.commons.compress.compressors.XZTestCase::testXZCreation
    public void testXZCreation()  throws Exception {
        final long max = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
        System.out.println("XZTestCase: HeapMax="+max+" bytes "+(double)max/(1024*1024)+" MB");
        final File input = getFile("test1.xml");
        final File output = new File(dir, "test1.xml.xz");
        try (OutputStream out = new FileOutputStream(output)) {
            try (CompressorOutputStream cos = new CompressorStreamFactory()
                    .createCompressorOutputStream("xz", out)) {
                IOUtils.copy(new FileInputStream(input), cos);
            }
        }
    }

// org.apache.commons.compress.compressors.XZTestCase::testXZUnarchive
    public void testXZUnarchive() throws Exception {
        final File input = getFile("bla.tar.xz");
        final File output = new File(dir, "bla.tar");
        try (InputStream is = new FileInputStream(input)) {
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
        }
    }

// org.apache.commons.compress.compressors.XZTestCase::testConcatenatedStreamsReadFirstOnly
    public void testConcatenatedStreamsReadFirstOnly() throws Exception {
        final File input = getFile("multiple.xz");
        try (InputStream is = new FileInputStream(input)) {
            try (CompressorInputStream in = new CompressorStreamFactory()
                    .createCompressorInputStream("xz", is)) {
                assertEquals('a', in.read());
                assertEquals(-1, in.read());
            }
        }
    }

// org.apache.commons.compress.compressors.XZTestCase::testConcatenatedStreamsReadFully
    public void testConcatenatedStreamsReadFully() throws Exception {
        final File input = getFile("multiple.xz");
        try (InputStream is = new FileInputStream(input)) {
            try (CompressorInputStream in = new XZCompressorInputStream(is, true)) {
                assertEquals('a', in.read());
                assertEquals('b', in.read());
                assertEquals(0, in.available());
                assertEquals(-1, in.read());
            }
        }
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

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::testBrotliDecode
    public void testBrotliDecode() throws IOException {
        final File input = getFile("brotli.testdata.compressed");
        final File expected = getFile("brotli.testdata.uncompressed");
        try (InputStream inputStream = new FileInputStream(input);
                InputStream expectedStream = new FileInputStream(expected);
                BrotliCompressorInputStream brotliInputStream = new BrotliCompressorInputStream(inputStream)) {
            final byte[] b = new byte[20];
            IOUtils.readFully(expectedStream, b);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int readByte = -1;
            while((readByte = brotliInputStream.read()) != -1) {
                bos.write(readByte);
            }
            Assert.assertArrayEquals(b, bos.toByteArray());
        } 
    }

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::testCachingIsEnabledByDefaultAndBrotliIsPresent
    public void testCachingIsEnabledByDefaultAndBrotliIsPresent() {
        assertEquals(BrotliUtils.CachedAvailability.CACHED_AVAILABLE, BrotliUtils.getCachedBrotliAvailability());
        assertTrue(BrotliUtils.isBrotliCompressionAvailable());
    }

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::testCanTurnOffCaching
    public void testCanTurnOffCaching() {
        try {
            BrotliUtils.setCacheBrotliAvailablity(false);
            assertEquals(BrotliUtils.CachedAvailability.DONT_CACHE, BrotliUtils.getCachedBrotliAvailability());
            assertTrue(BrotliUtils.isBrotliCompressionAvailable());
        } finally {
            BrotliUtils.setCacheBrotliAvailablity(true);
        }
    }

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::testTurningOnCachingReEvaluatesAvailability
    public void testTurningOnCachingReEvaluatesAvailability() {
        try {
            BrotliUtils.setCacheBrotliAvailablity(false);
            assertEquals(BrotliUtils.CachedAvailability.DONT_CACHE, BrotliUtils.getCachedBrotliAvailability());
            BrotliUtils.setCacheBrotliAvailablity(true);
            assertEquals(BrotliUtils.CachedAvailability.CACHED_AVAILABLE, BrotliUtils.getCachedBrotliAvailability());
        } finally {
            BrotliUtils.setCacheBrotliAvailablity(true);
        }
    }

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::availableShouldReturnZero
    public void availableShouldReturnZero() throws IOException {
        final File input = getFile("brotli.testdata.compressed");
        try (InputStream is = new FileInputStream(input)) {
            final BrotliCompressorInputStream in =
                    new BrotliCompressorInputStream(is);
            Assert.assertTrue(in.available() == 0);
            in.close();
        }
    }

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::shouldBeAbleToSkipAByte
    public void shouldBeAbleToSkipAByte() throws IOException {
        final File input = getFile("brotli.testdata.compressed");
        try (InputStream is = new FileInputStream(input)) {
            final BrotliCompressorInputStream in =
                    new BrotliCompressorInputStream(is);
            Assert.assertEquals(1, in.skip(1));
            in.close();
        }
    }

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::singleByteReadWorksAsExpected
    public void singleByteReadWorksAsExpected() throws IOException {
        final File input = getFile("brotli.testdata.compressed");
        try (InputStream is = new FileInputStream(input)) {
            final BrotliCompressorInputStream in =
                    new BrotliCompressorInputStream(is);
            
            Assert.assertEquals('X', in.read());
            in.close();
        }
    }

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::singleByteReadReturnsMinusOneAtEof
    public void singleByteReadReturnsMinusOneAtEof() throws IOException {
        final File input = getFile("brotli.testdata.compressed");
        try (InputStream is = new FileInputStream(input)) {
            final BrotliCompressorInputStream in =
                    new BrotliCompressorInputStream(is);
            IOUtils.toByteArray(in);
            Assert.assertEquals(-1, in.read());
            in.close();
        }
    }

// org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStreamTest::testBrotliUnarchive
    public void testBrotliUnarchive() throws Exception {
        final File input = getFile("bla.tar.br");
        final File output = new File(dir, "bla.tar");
        try (InputStream is = new FileInputStream(input)) {
            final CompressorInputStream in = new CompressorStreamFactory()
                    .createCompressorInputStream("br", is);
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
        }
    }

// org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStreamTest::readBlaLz4
    public void readBlaLz4() throws IOException {
        try (InputStream a = new BlockLZ4CompressorInputStream(new FileInputStream(getFile("bla.tar.block_lz4")));
            FileInputStream e = new FileInputStream(getFile("bla.tar"))) {
            byte[] expected = IOUtils.toByteArray(e);
            byte[] actual = IOUtils.toByteArray(a);
            Assert.assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorRoundtripTest::blaTarRoundtrip
    public void blaTarRoundtrip() throws IOException {
        roundTripTest("bla.tar");
    }

// org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorRoundtripTest::gzippedLoremIpsumRoundtrip
    public void gzippedLoremIpsumRoundtrip() throws IOException {
        roundTripTest("lorem-ipsum.txt.gz");
    }

// org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorRoundtripTest::biggerFileRoundtrip
    public void biggerFileRoundtrip() throws IOException {
        roundTripTest("COMPRESS-256.7z");
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
            assertEquals(0, in.available()); 
            assertEquals(4, in.read(new byte[5], 0, 4));
            assertEquals('5', in.read());
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

// org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStreamTest::readIWAFileWithBiggerOffset
    public void readIWAFileWithBiggerOffset() throws Exception {
        File o = new File(dir, "COMPRESS-358.raw");
        try (InputStream is = new FileInputStream(getFile("COMPRESS-358.iwa"));
             FramedSnappyCompressorInputStream in =
                 new FramedSnappyCompressorInputStream(is, 1<<16, FramedSnappyDialect.IWORK_ARCHIVE);
             FileOutputStream out = new FileOutputStream(o)) {
            IOUtils.copy(in, out);
        }
        try (FileInputStream a = new FileInputStream(o);
             FileInputStream e = new FileInputStream(getFile("COMPRESS-358.uncompressed"))) {
            byte[] expected = IOUtils.toByteArray(e);
            byte[] actual = IOUtils.toByteArray(a);
            assertArrayEquals(expected, actual);
        }
    }

// org.apache.commons.compress.compressors.snappy.SnappyRoundtripTest::blaTarRoundtrip
    public void blaTarRoundtrip() throws IOException {
        System.err.println("Configuration: default");
        roundTripTest("bla.tar");
    }

// org.apache.commons.compress.compressors.snappy.SnappyRoundtripTest::blaTarRoundtripTunedForSpeed
    public void blaTarRoundtripTunedForSpeed() throws IOException {
        System.err.println("Configuration: tuned for speed");
        roundTripTest(getFile("bla.tar"),
            SnappyCompressorOutputStream.createParameterBuilder(SnappyCompressorInputStream.DEFAULT_BLOCK_SIZE)
                .tunedForSpeed()
                .build());
    }

// org.apache.commons.compress.compressors.snappy.SnappyRoundtripTest::blaTarRoundtripTunedForCompressionRatio
    public void blaTarRoundtripTunedForCompressionRatio() throws IOException {
        System.err.println("Configuration: tuned for compression ratio");
        roundTripTest(getFile("bla.tar"),
            SnappyCompressorOutputStream.createParameterBuilder(SnappyCompressorInputStream.DEFAULT_BLOCK_SIZE)
                .tunedForCompressionRatio()
                .build());
    }

// org.apache.commons.compress.compressors.snappy.SnappyRoundtripTest::gzippedLoremIpsumRoundtrip
    public void gzippedLoremIpsumRoundtrip() throws IOException {
        roundTripTest("lorem-ipsum.txt.gz");
    }

// org.apache.commons.compress.compressors.snappy.SnappyRoundtripTest::biggerFileRoundtrip
    public void biggerFileRoundtrip() throws IOException {
        roundTripTest("COMPRESS-256.7z");
    }

// org.apache.commons.compress.compressors.snappy.SnappyRoundtripTest::tryReallyBigOffset
    public void tryReallyBigOffset() throws IOException {
        
        
        
        
        
        
        
        
        
        
        File f = new File(dir, "reallyBigOffsetTest");
        try (FileOutputStream fs = new FileOutputStream(f)) {
            fs.write(0);
            fs.write(0);
            fs.write(0);
            fs.write(0);
            int cnt = 1 << 16 + 5;
            Random r = new Random();
            for (int i = 0 ; i < cnt; i++) {
                fs.write(r.nextInt(255) + 1);
            }
            fs.write(0);
            fs.write(0);
            fs.write(0);
            fs.write(0);
        }
        roundTripTest(f, newParameters(1 << 17, 4, 64, 1 << 17 - 1, 1 << 17 - 1));
    }

// org.apache.commons.compress.compressors.snappy.SnappyRoundtripTest::tryReallyLongLiterals
    public void tryReallyLongLiterals() throws IOException {
        
        
        
        
        
        
        
        
        
        
        
        
        
        File f = new File(dir, "reallyBigLiteralTest");
        try (FileOutputStream fs = new FileOutputStream(f)) {
            int cnt = 1 << 19;
            Random r = new Random();
            for (int i = 0 ; i < cnt; i++) {
                fs.write(r.nextInt(256));
            }
        }
        roundTripTest(f, newParameters(1 << 18, 4, 64, 1 << 16 - 1, 1 << 18 - 1));
    }
