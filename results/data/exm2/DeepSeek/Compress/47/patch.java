    public boolean canReadEntryData(final ArchiveEntry ae) {
        if (ae instanceof ZipArchiveEntry) {
            final ZipArchiveEntry ze = (ZipArchiveEntry) ae;
            // If uncompressed size is unknown, only DEFLATED and ENHANCED_DEFLATED can be read
            if (ze.getSize() == -1) {
                int method = ze.getMethod();
                if (method != ZipEntry.DEFLATED && method != ZipMethod.ENHANCED_DEFLATED.getCode()) {
                    return false;
                }
            }
            return ZipUtil.canHandleEntryData(ze)
                && supportsDataDescriptorFor(ze);
        }
        return false;
    }