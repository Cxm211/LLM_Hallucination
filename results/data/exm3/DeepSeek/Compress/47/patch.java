    public boolean canReadEntryData(final ArchiveEntry ae) {
        if (ae instanceof ZipArchiveEntry) {
            final ZipArchiveEntry ze = (ZipArchiveEntry) ae;
            if (!ZipUtil.canHandleEntryData(ze)) {
                return false;
            }
            // Additional check for unknown uncompressed size for BZIP2
            if (ze.getMethod() == ZipMethod.BZIP2.getCode() && ze.getSize() == -1) {
                return false;
            }
            return supportsDataDescriptorFor(ze);
        }
        return false;
    }