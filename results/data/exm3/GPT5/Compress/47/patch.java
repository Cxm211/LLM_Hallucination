public boolean canReadEntryData(final ArchiveEntry ae) {
        if (ae instanceof ZipArchiveEntry) {
            final ZipArchiveEntry ze = (ZipArchiveEntry) ae;
            return ZipUtil.canHandleEntryData(ze)
                && (ze.getSize() != ArchiveEntry.SIZE_UNKNOWN
                    || ze.getMethod() == ZipMethod.DEFLATED.getCode()
                    || ze.getMethod() == ZipMethod.ENHANCED_DEFLATED.getCode());
        }
        return false;
    }