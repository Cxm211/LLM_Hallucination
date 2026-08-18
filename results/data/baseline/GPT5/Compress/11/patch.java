public ArchiveInputStream createArchiveInputStream(final InputStream in)
            throws ArchiveException {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }

        final InputStream in2 = in.markSupported() ? in : new BufferedInputStream(in);

        final byte[] signature = new byte[12];
        in2.mark(signature.length);
        try {
            int signatureLength = in2.read(signature);
            in2.reset();
            if (ZipArchiveInputStream.matches(signature, signatureLength)) {
                return new ZipArchiveInputStream(in2);
            } else if (JarArchiveInputStream.matches(signature, signatureLength)) {
                return new JarArchiveInputStream(in2);
            } else if (ArArchiveInputStream.matches(signature, signatureLength)) {
                return new ArArchiveInputStream(in2);
            } else if (CpioArchiveInputStream.matches(signature, signatureLength)) {
                return new CpioArchiveInputStream(in2);
            }

            // Dump needs a bigger buffer to check the signature;
            final byte[] dumpsig = new byte[32];
            in2.mark(dumpsig.length);
            signatureLength = in2.read(dumpsig);
            in2.reset();
            if (DumpArchiveInputStream.matches(dumpsig, signatureLength)) {
                return new DumpArchiveInputStream(in2);
            }

            // Tar needs an even bigger buffer to check the signature; read the first block
            final byte[] tarheader = new byte[512];
            in2.mark(tarheader.length);
            signatureLength = in2.read(tarheader);
            in2.reset();
            if (TarArchiveInputStream.matches(tarheader, signatureLength)) {
                return new TarArchiveInputStream(in2);
            }
            // COMPRESS-117 - improve auto-recognition
            try {
                TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarheader));
                tais.getNextEntry();
                return new TarArchiveInputStream(in2);
            } catch (Exception e) { // NOPMD
                // can generate IllegalArgumentException as well as IOException
                // autodetection, simply not a TAR
                // ignored
            }
        } catch (IOException e) {
            throw new ArchiveException("Could not use reset and mark operations.", e);
        }

        throw new ArchiveException("No Archiver found for the stream signature");
    }