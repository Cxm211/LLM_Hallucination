public ArchiveInputStream createArchiveInputStream(final InputStream in)
            throws ArchiveException {
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
            int signatureLength = input.read(signature);
            input.reset();
            if (ZipArchiveInputStream.matches(signature, signatureLength)) {
                return new ZipArchiveInputStream(input);
            } else if (JarArchiveInputStream.matches(signature, signatureLength)) {
                return new JarArchiveInputStream(input);
            } else if (ArArchiveInputStream.matches(signature, signatureLength)) {
                return new ArArchiveInputStream(input);
            } else if (CpioArchiveInputStream.matches(signature, signatureLength)) {
                return new CpioArchiveInputStream(input);
            }

            // Dump needs a bigger buffer to check the signature;
            final byte[] dumpsig = new byte[32];
            input.mark(dumpsig.length);
            signatureLength = input.read(dumpsig);
            input.reset();
            if (DumpArchiveInputStream.matches(dumpsig, signatureLength)) {
                return new DumpArchiveInputStream(input);
            }

            // Tar needs an even bigger buffer to check the signature; read the first block
            final byte[] tarheader = new byte[512];
            input.mark(tarheader.length);
            signatureLength = input.read(tarheader);
            input.reset();
            if (TarArchiveInputStream.matches(tarheader, signatureLength)) {
                return new TarArchiveInputStream(input);
            }
            // COMPRESS-117 - improve auto-recognition
            if (signatureLength >= 512) {
                try {
                    TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarheader));
                    // COMPRESS-191 - verify the header checksum
                    tais.getNextEntry();
                        return new TarArchiveInputStream(input);
                } catch (Exception e) { // NOPMD
                    // can generate IllegalArgumentException as well
                    // as IOException
                    // autodetection, simply not a TAR
                    // ignored
                }
            }
        } catch (IOException e) {
            throw new ArchiveException("Could not use reset and mark operations.", e);
        }

        throw new ArchiveException("No Archiver found for the stream signature");
    }