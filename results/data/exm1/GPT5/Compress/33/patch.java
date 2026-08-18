public CompressorInputStream createCompressorInputStream(final InputStream in)
            throws CompressorException {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }

        InputStream source = in;
        if (!source.markSupported()) {
            source = new BufferedInputStream(source);
        }

        final byte[] signature = new byte[12];
        source.mark(signature.length);
        try {
            int signatureLength = IOUtils.readFully(source, signature);
            source.reset();

            if (BZip2CompressorInputStream.matches(signature, signatureLength)) {
                return new BZip2CompressorInputStream(source, decompressConcatenated);
            }

            if (GzipCompressorInputStream.matches(signature, signatureLength)) {
                return new GzipCompressorInputStream(source, decompressConcatenated);
            }

            if (Pack200CompressorInputStream.matches(signature, signatureLength)) {
                return new Pack200CompressorInputStream(source);
            }

            if (FramedSnappyCompressorInputStream.matches(signature, signatureLength)) {
                return new FramedSnappyCompressorInputStream(source);
            }

            if (ZCompressorInputStream.matches(signature, signatureLength)) {
                return new ZCompressorInputStream(source);
            }


            if (XZUtils.matches(signature, signatureLength) &&
                XZUtils.isXZCompressionAvailable()) {
                return new XZCompressorInputStream(source, decompressConcatenated);
            }

            if (LZMAUtils.matches(signature, signatureLength) &&
                LZMAUtils.isLZMACompressionAvailable()) {
                return new LZMACompressorInputStream(source);
            }

        } catch (IOException e) {
            throw new CompressorException("Failed to detect Compressor from InputStream.", e);
        }

        throw new CompressorException("No Compressor found for the stream signature.");
    }