private boolean supportsCompressedSizeFor(final ZipArchiveEntry entry) {
    return entry.getCompressedSize() != ArchiveEntry.SIZE_UNKNOWN
            || entry.getMethod() == ZipEntry.DEFLATED
            || entry.getMethod() == ZipArchiveOutputStream.STORED;
}