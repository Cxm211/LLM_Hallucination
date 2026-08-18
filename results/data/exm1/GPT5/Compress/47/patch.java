public boolean canReadEntryData(final ArchiveEntry ae) {
        if (ae instanceof ZipArchiveEntry) {
            final ZipArchiveEntry ze = (ZipArchiveEntry) ae;
            if (ze.getMethod() == ZipMethod.BZIP2.getCode() && ze.getSize() < 0) {
                return false;
            }
            return ZipUtil.canHandleEntryData(ze)
                && supportsDataDescriptorFor(ze);
        }
        return false;
    }