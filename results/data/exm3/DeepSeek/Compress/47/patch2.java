    private boolean supportsDataDescriptorFor(final ZipArchiveEntry entry) {
        return !entry.getGeneralPurposeBit().usesDataDescriptor()

                || (allowStoredEntriesWithDataDescriptor && entry.getMethod() == ZipArchiveOutputStream.STORED)
                || entry.getMethod() == ZipArchiveOutputStream.DEFLATED
                || entry.getMethod() == ZipMethod.ENHANCED_DEFLATED.getCode();
    }