public ArchiveInputStream createArchiveInputStream(final InputStream in)
            throws ArchiveException {
        if (in == null) {
            throw new IllegalArgumentException("Stream must not be null.");
        }

        // Wrap the stream to support mark/reset if necessary
        final InputStream checkedIn = in.markSupported() ? in : new BufferedInputStream(in);

        final byte[] signature = new byte[12];
        checkedIn.mark(signature.length);
        try {
            int signatureLength = checkedIn.read(signature);
            checkedIn.reset();
            if (ZipArchiveInputStream.matches(signature, signatureLength)) {
                return new ZipArchiveInputStream(checkedIn);
            } else if (JarArchiveInputStream.matches(signature, signatureLength)) {
                return new JarArchiveInputStream(checkedIn);
            } else if (ArArchiveInputStream.matches(signature, signatureLength)) {
                return new ArArchiveInputStream(checkedIn);
            } else if (CpioArchiveInputStream.matches(signature, signatureLength)) {
                return new CpioArchiveInputStream(checkedIn);
            }

            // Dump needs a bigger buffer to check the signature;
            final byte[] dumpsig = new byte[32];
            checkedIn.mark(dumpsig.length);
            signatureLength = checkedIn.read(dumpsig);
            checkedIn.reset();
            if (DumpArchiveInputStream.matches(dumpsig, signatureLength)) {
                return new DumpArchiveInputStream(checkedIn);
            }

            // Tar needs an even bigger buffer to check the signature; read the first block
            final byte[] tarheader = new byte[512];
            checkedIn.mark(tarheader.length);
            signatureLength = checkedIn.read(tarheader);
            checkedIn.reset();
            if (TarArchiveInputStream.matches(tarheader, signatureLength)) {
                return new TarArchiveInputStream(checkedIn);
            }
            // COMPRESS-117 - improve auto-recognition
            try {
                TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarheader));
                tais.getNextEntry();
                return new TarArchiveInputStream(checkedIn);
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