public ZipArchiveInputStream(InputStream inputStream,
                             String encoding,
                             boolean useUnicodeExtraFields,
                             boolean allowStoredEntriesWithDataDescriptor) {
    zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
    this.useUnicodeExtraFields = useUnicodeExtraFields;
    buf = ByteBuffer.allocate(ZipArchiveOutputStream.BUFFER_SIZE);
    in = new PushbackInputStream(inputStream, buf.capacity());
    this.allowStoredEntriesWithDataDescriptor =
        allowStoredEntriesWithDataDescriptor;
    // haven't read anything so far
}