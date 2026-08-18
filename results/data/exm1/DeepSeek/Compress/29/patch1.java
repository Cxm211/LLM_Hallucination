public ArchiveInputStream createArchiveInputStream(final InputStream in) throws ArchiveException {
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
        final byte[] dumpsig = new byte[32];
        in.mark(dumpsig.length);
        signatureLength = IOUtils.readFully(in, dumpsig);
        in.reset();
        if (DumpArchiveInputStream.matches(dumpsig, signatureLength)) {
            return new DumpArchiveInputStream(in, entryEncoding);
        }
        final byte[] tarheader = new byte[512];
        in.mark(tarheader.length);
        signatureLength = IOUtils.readFully(in, tarheader);
        in.reset();
        if (TarArchiveInputStream.matches(tarheader, signatureLength)) {
            return new TarArchiveInputStream(in, entryEncoding);
        }
        if (signatureLength >= 512) {
            TarArchiveInputStream tais = null;
            try {
                tais = new TarArchiveInputStream(new ByteArrayInputStream(tarheader));
                if (tais.getNextTarEntry().isCheckSumOK()) {
                    return new TarArchiveInputStream(in, entryEncoding);
                }
            } catch (Exception e) {
            } finally {
                IOUtils.closeQuietly(tais);
            }
        }
    } catch (IOException e) {
        throw new ArchiveException("Could not use reset and mark operations.", e);
    }
    throw new ArchiveException("No Archiver found for the stream signature");
}