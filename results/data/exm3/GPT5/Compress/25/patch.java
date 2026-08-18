public ZipArchiveInputStream(InputStream inputStream,
                                 String encoding,
                                 boolean useUnicodeExtraFields,
                                 boolean allowStoredEntriesWithDataDescriptor) {
        zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        final int defaultPushbackBufferSize = 512;
        InputStream buffered = (inputStream instanceof BufferedInputStream)
            ? inputStream
            : new BufferedInputStream(inputStream);
        in = new PushbackInputStream(buffered, defaultPushbackBufferSize);
        this.allowStoredEntriesWithDataDescriptor =
            allowStoredEntriesWithDataDescriptor;
        // haven't read anything so far
    }