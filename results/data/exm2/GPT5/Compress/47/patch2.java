private boolean supportsDataDescriptorFor(final ZipArchiveEntry entry) {
        final boolean usesDataDescriptor = entry.getGeneralPurposeBit().usesDataDescriptor()
            || entry.getSize() == ArchiveEntry.SIZE_UNKNOWN;
        if (!usesDataDescriptor) {
            return true;
        }
        return (allowStoredEntriesWithDataDescriptor && entry.getMethod() == ZipEntry.STORED)
            || entry.getMethod() == ZipEntry.DEFLATED
            || entry.getMethod() == ZipMethod.ENHANCED_DEFLATED.getCode();
    }