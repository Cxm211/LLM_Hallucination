// buggy code
    public ArchiveOutputStream createArchiveOutputStream(
            final String archiverName, final OutputStream out)
            throws ArchiveException {
        if (archiverName == null) {
            throw new IllegalArgumentException("Archivername must not be null.");
        }
        if (out == null) {
            throw new IllegalArgumentException("OutputStream must not be null.");
        }

        if (AR.equalsIgnoreCase(archiverName)) {
            return new ArArchiveOutputStream(out);
        }
        if (ZIP.equalsIgnoreCase(archiverName)) {
            ZipArchiveOutputStream zip = new ZipArchiveOutputStream(out);
            if (entryEncoding != null) {
                zip.setEncoding(entryEncoding);
            }
            return zip;
        }
        if (TAR.equalsIgnoreCase(archiverName)) {
            if (entryEncoding != null) {
                return new TarArchiveOutputStream(out, entryEncoding);
            } else {
                return new TarArchiveOutputStream(out);
            }
        }
        if (JAR.equalsIgnoreCase(archiverName)) {
                return new JarArchiveOutputStream(out);
        }
        if (CPIO.equalsIgnoreCase(archiverName)) {
            if (entryEncoding != null) {
                return new CpioArchiveOutputStream(out, entryEncoding);
            } else {
                return new CpioArchiveOutputStream(out);
            }
        }
        if (SEVEN_Z.equalsIgnoreCase(archiverName)) {
            throw new StreamingNotSupportedException(SEVEN_Z);
        }
        throw new ArchiveException("Archiver: " + archiverName + " not found.");
    }

    public ArchiveInputStream createArchiveInputStream(final InputStream in)
            throws ArchiveException {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }

        if (!in.markSupported()) {
            throw new IllegalArgumentException("Mark is not supported.");
        }

        final byte[] signature = new byte[12];
        in.mark(signature.length);
        try {
            int signatureLength = IOUtils.readFully(in, signature);
            in.reset();
            if (ZipArchiveInputStream.matches(signature, signatureLength)) {
                if (entryEncoding != null) {
                    return new ZipArchiveInputStream(in, entryEncoding);
                } else {
                    return new ZipArchiveInputStream(in);
                }
            } else if (JarArchiveInputStream.matches(signature, signatureLength)) {
                if (entryEncoding != null) {
                    return new JarArchiveInputStream(in, entryEncoding);
                } else {
                    return new JarArchiveInputStream(in);
                }
            } else if (ArArchiveInputStream.matches(signature, signatureLength)) {
                return new ArArchiveInputStream(in);
            } else if (CpioArchiveInputStream.matches(signature, signatureLength)) {
                if (entryEncoding != null) {
                    return new CpioArchiveInputStream(in, entryEncoding);
                } else {
                    return new CpioArchiveInputStream(in);
                }
            } else if (ArjArchiveInputStream.matches(signature, signatureLength)) {
                    return new ArjArchiveInputStream(in);
            } else if (SevenZFile.matches(signature, signatureLength)) {
                throw new StreamingNotSupportedException(SEVEN_Z);
            }

            // Dump needs a bigger buffer to check the signature;
            final byte[] dumpsig = new byte[32];
            in.mark(dumpsig.length);
            signatureLength = IOUtils.readFully(in, dumpsig);
            in.reset();
            if (DumpArchiveInputStream.matches(dumpsig, signatureLength)) {
                return new DumpArchiveInputStream(in, entryEncoding);
            }

            // Tar needs an even bigger buffer to check the signature; read the first block
            final byte[] tarheader = new byte[512];
            in.mark(tarheader.length);
            signatureLength = IOUtils.readFully(in, tarheader);
            in.reset();
            if (TarArchiveInputStream.matches(tarheader, signatureLength)) {
                return new TarArchiveInputStream(in, entryEncoding);
            }
            // COMPRESS-117 - improve auto-recognition
            if (signatureLength >= 512) {
                TarArchiveInputStream tais = null;
                try {
                    tais = new TarArchiveInputStream(new ByteArrayInputStream(tarheader));
                    // COMPRESS-191 - verify the header checksum
                    if (tais.getNextTarEntry().isCheckSumOK()) {
                        return new TarArchiveInputStream(in, encoding);
                    }
                } catch (Exception e) { // NOPMD
                    // can generate IllegalArgumentException as well
                    // as IOException
                    // autodetection, simply not a TAR
                    // ignored
                } finally {
                    IOUtils.closeQuietly(tais);
                }
            }
        } catch (IOException e) {
            throw new ArchiveException("Could not use reset and mark operations.", e);
        }

        throw new ArchiveException("No Archiver found for the stream signature");
    }

    public CpioArchiveInputStream(final InputStream in, int blockSize, String encoding) {
        this.in = in;
        this.blockSize = blockSize;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
    }

    public CpioArchiveOutputStream(final OutputStream out, final short format,
                                   final int blockSize, final String encoding) {
        this.out = out;
        switch (format) {
        case FORMAT_NEW:
        case FORMAT_NEW_CRC:
        case FORMAT_OLD_ASCII:
        case FORMAT_OLD_BINARY:
            break;
        default:
            throw new IllegalArgumentException("Unknown format: "+format);

        }
        this.entryFormat = format;
        this.blockSize = blockSize;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
    }

    public DumpArchiveInputStream(InputStream is, String encoding)
        throws ArchiveException {
        this.raw = new TapeInputStream(is);
        this.hasHitEOF = false;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);

        try {
            // read header, verify it's a dump archive.
            byte[] headerBytes = raw.readRecord();

            if (!DumpArchiveUtil.verify(headerBytes)) {
                throw new UnrecognizedFormatException();
            }

            // get summary information
            summary = new DumpArchiveSummary(headerBytes, this.zipEncoding);

            // reset buffer with actual block size.
            raw.resetBlockSize(summary.getNTRec(), summary.isCompressed());

            // allocate our read buffer.
            blockBuffer = new byte[4 * DumpArchiveConstants.TP_SIZE];

            // skip past CLRI and BITS segments since we don't handle them yet.
            readCLRI();
            readBITS();
        } catch (IOException ex) {
            throw new ArchiveException(ex.getMessage(), ex);
        }

        // put in a dummy record for the root node.
        Dirent root = new Dirent(2, 2, 4, ".");
        names.put(2, root);

        // use priority based on queue to ensure parent directories are
        // released first.
        queue = new PriorityQueue<DumpArchiveEntry>(10,
                new Comparator<DumpArchiveEntry>() {
                    public int compare(DumpArchiveEntry p, DumpArchiveEntry q) {
                        if (p.getOriginalName() == null || q.getOriginalName() == null) {
                            return Integer.MAX_VALUE;
                        }

                        return p.getOriginalName().compareTo(q.getOriginalName());
                    }
                });
    }

    public TarArchiveInputStream(InputStream is, int blockSize, int recordSize,
                                 String encoding) {
        this.is = is;
        this.hasHitEOF = false;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.recordSize = recordSize;
        this.blockSize = blockSize;
    }

    public TarArchiveOutputStream(OutputStream os, int blockSize,
                                  int recordSize, String encoding) {
        out = new CountingOutputStream(os);
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);

        this.assemLen = 0;
        this.assemBuf = new byte[recordSize];
        this.recordBuf = new byte[recordSize];
        this.recordSize = recordSize;
        this.recordsPerBlock = blockSize / recordSize;
    }

    public ZipArchiveInputStream(InputStream inputStream,
                                 String encoding,
                                 boolean useUnicodeExtraFields,
                                 boolean allowStoredEntriesWithDataDescriptor) {
        zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        in = new PushbackInputStream(inputStream, buf.capacity());
        this.allowStoredEntriesWithDataDescriptor =
            allowStoredEntriesWithDataDescriptor;
        // haven't read anything so far
        buf.limit(0);
    }

// relevant test
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
            in.close();
        } catch (IOException ex) {
            assertTrue(ex.getMessage().contains("unskippable chunk"));
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
