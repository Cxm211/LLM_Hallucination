    protected void writeDataDescriptor(final ZipArchiveEntry ze) throws IOException {
        if (ze.getMethod() != DEFLATED || channel != null) {
            return;
        }
        // Only write a data descriptor if sizes/CRC weren't known when writing the LFH
        if (ze.getCrc() != -1 && ze.getSize() != -1 && ze.getCompressedSize() != -1) {
            return;
        }
        writeCounted(DD_SIG);
        writeCounted(ZipLong.getBytes(ze.getCrc()));
        if (!hasZip64Extra(ze)) {
            writeCounted(ZipLong.getBytes(ze.getCompressedSize()));
            writeCounted(ZipLong.getBytes(ze.getSize()));
        } else {
            writeCounted(ZipEightByteInteger.getBytes(ze.getCompressedSize()));
            writeCounted(ZipEightByteInteger.getBytes(ze.getSize()));
        }
    }