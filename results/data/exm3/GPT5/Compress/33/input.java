// buggy function
    public CompressorInputStream createCompressorInputStream(final InputStream in)
            throws CompressorException {
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

            if (BZip2CompressorInputStream.matches(signature, signatureLength)) {
                return new BZip2CompressorInputStream(in, decompressConcatenated);
            }

            if (GzipCompressorInputStream.matches(signature, signatureLength)) {
                return new GzipCompressorInputStream(in, decompressConcatenated);
            }

            if (Pack200CompressorInputStream.matches(signature, signatureLength)) {
                return new Pack200CompressorInputStream(in);
            }

            if (FramedSnappyCompressorInputStream.matches(signature, signatureLength)) {
                return new FramedSnappyCompressorInputStream(in);
            }

            if (ZCompressorInputStream.matches(signature, signatureLength)) {
                return new ZCompressorInputStream(in);
            }


            if (XZUtils.matches(signature, signatureLength) &&
                XZUtils.isXZCompressionAvailable()) {
                return new XZCompressorInputStream(in, decompressConcatenated);
            }

            if (LZMAUtils.matches(signature, signatureLength) &&
                LZMAUtils.isLZMACompressionAvailable()) {
                return new LZMACompressorInputStream(in);
            }

        } catch (IOException e) {
            throw new CompressorException("Failed to detect Compressor from InputStream.", e);
        }

        throw new CompressorException("No Compressor found for the stream signature.");
    }

    public void close() throws IOException {
        in.close();
    }

// trigger testcase
// org/apache/commons/compress/compressors/DetectCompressorTestCase.java::testDetection
@Test
    public void testDetection() throws Exception {
        CompressorInputStream bzip2 = getStreamFor("bla.txt.bz2"); 
        assertNotNull(bzip2);
        assertTrue(bzip2 instanceof BZip2CompressorInputStream);

        CompressorInputStream gzip = getStreamFor("bla.tgz");
        assertNotNull(gzip);
        assertTrue(gzip instanceof GzipCompressorInputStream);
        
        CompressorInputStream pack200 = getStreamFor("bla.pack");
        assertNotNull(pack200);
        assertTrue(pack200 instanceof Pack200CompressorInputStream);

        CompressorInputStream xz = getStreamFor("bla.tar.xz");
        assertNotNull(xz);
        assertTrue(xz instanceof XZCompressorInputStream);

        CompressorInputStream zlib = getStreamFor("bla.tar.deflatez");
        assertNotNull(zlib);
        assertTrue(zlib instanceof DeflateCompressorInputStream);

        try {
            factory.createCompressorInputStream(new ByteArrayInputStream(new byte[0]));
            fail("No exception thrown for an empty input stream");
        } catch (CompressorException e) {
            // expected
        }
    }
