public ZipArchiveInputStream(InputStream inputStream,
                                 String encoding,
                                 boolean useUnicodeExtraFields,
                                 boolean allowStoredEntriesWithDataDescriptor) {
        zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        final int pushbackBufferSize = 512;
        in = new PushbackInputStream(inputStream, pushbackBufferSize);
        this.allowStoredEntriesWithDataDescriptor =
            allowStoredEntriesWithDataDescriptor;
        // haven't read anything so far
    }