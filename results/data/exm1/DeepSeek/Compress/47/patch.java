public boolean canReadEntryData(final ArchiveEntry ae) {
    if (ae instanceof ZipArchiveEntry) {
        final ZipArchiveEntry ze = (ZipArchiveEntry) ae;
        if (!ZipUtil.canHandleEntryData(ze)) {
            return false;
        }
        if (!supportsDataDescriptorFor(ze)) {
            return false;
        }
        if (ze.getMethod() != ZipEntry.STORED &&
            ze.getMethod() != ZipEntry.DEFLATED &&
            ze.getMethod() != ZipMethod.ENHANCED_DEFLATED.getCode()) {
            if (ze.getSize() == -1) {
                return false;
            }
        }
        return true;
    }
    return false;
}