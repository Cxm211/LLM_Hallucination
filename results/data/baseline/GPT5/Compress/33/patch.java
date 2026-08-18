public CompressorInputStream createCompressorInputStream(final InputStream in)
            throws CompressorException {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }

        InputStream input = in;
        if (!input.markSupported()) {
            input = new BufferedInputStream(input);
        }

        final byte[] signature = new byte[12];
        input.mark(signature.length);
        try {
            int signatureLength = IOUtils.readFully(input, signature);
            input.reset();

            if (BZip2CompressorInputStream.matches(signature, signatureLength)) {
                return new BZip2CompressorInputStream(input, decompressConcatenated);
            }

            if (GzipCompressorInputStream.matches(signature, signatureLength)) {
                return new GzipCompressorInputStream(input, decompressConcatenated);
            }

            if (Pack200CompressorInputStream.matches(signature, signatureLength)) {
                return new Pack200CompressorInputStream(input);
            }

            if (FramedSnappyCompressorInputStream.matches(signature, signatureLength)) {
                return new FramedSnappyCompressorInputStream(input);
            }

            if (ZCompressorInputStream.matches(signature, signatureLength)) {
                return new ZCompressorInputStream(input);
            }


            if (XZUtils.matches(signature, signatureLength) &&
                XZUtils.isXZCompressionAvailable()) {
                return new XZCompressorInputStream(input, decompressConcatenated);
            }

            if (LZMAUtils.matches(signature, signatureLength) &&
                LZMAUtils.isLZMACompressionAvailable()) {
                return new LZMACompressorInputStream(input);
            }

        } catch (IOException e) {
            throw new CompressorException("Failed to detect Compressor from InputStream.", e);
        }

        throw new CompressorException("No Compressor found for the stream signature.");
    }