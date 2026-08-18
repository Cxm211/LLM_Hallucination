public ArchiveInputStream createArchiveInputStream(
    final String archiverName, final InputStream in)
    throws ArchiveException {
    if (archiverName == null) {
        throw new IllegalArgumentException("Archivername must not be null.");
    }
    if (in == null) {
        throw new IllegalArgumentException("InputStream must not be null.");
    }

    if (AR.equalsIgnoreCase(archiverName)) {
        return new ArArchiveInputStream(in);
    }
    if (ZIP.equalsIgnoreCase(archiverName)) {
        if (entryEncoding != null) {
            return new ZipArchiveInputStream(in, entryEncoding);
        } else {
            return new ZipArchiveInputStream(in);
        }
    }
    if (TAR.equalsIgnoreCase(archiverName)) {
        if (entryEncoding != null) {
            return new TarArchiveInputStream(in, entryEncoding);
        } else {
            return new TarArchiveInputStream(in);
        }
    }
    if (JAR.equalsIgnoreCase(archiverName)) {
        if (entryEncoding != null) {
            return new JarArchiveInputStream(in, entryEncoding);
        } else {
            return new JarArchiveInputStream(in);
        }
    }
    if (CPIO.equalsIgnoreCase(archiverName)) {
        if (entryEncoding != null) {
            return new CpioArchiveInputStream(in, entryEncoding);
        } else {
            return new CpioArchiveInputStream(in);
        }
    }
    if (DUMP.equalsIgnoreCase(archiverName)) {
        if (entryEncoding != null) {
            return new DumpArchiveInputStream(in, entryEncoding);
        } else {
            return new DumpArchiveInputStream(in);
        }
    }
    if (ARJ.equalsIgnoreCase(archiverName)) {
        if (entryEncoding != null) {
            return new ArjArchiveInputStream(in, entryEncoding);
        } else {
            return new ArjArchiveInputStream(in);
        }
    }
    if (SEVEN_Z.equalsIgnoreCase(archiverName)) {
        throw new StreamingNotSupportedException(SEVEN_Z);
    }

    throw new ArchiveException("Archiver: " + archiverName + " not found.");
}