private boolean supportsDataDescriptorFor(final ZipArchiveEntry entry) {
    return !entry.getGeneralPurposeBit().usesDataDescriptor()

            || (allowStoredEntriesWithDataDescriptor && entry.getMethod() == ZipEntry.STORED)
            || entry.getMethod() == ZipEntry.DEFLATED
            || entry.getMethod() == ZipMethod.ENHANCED_DEFLATED.getCode()
            || entry.getMethod() == ZipMethod.BZIP2.getCode();
}