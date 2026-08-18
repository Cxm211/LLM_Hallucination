    public ZipArchiveInputStream(InputStream inputStream,
                                 String encoding,
                                 boolean useUnicodeExtraFields,
                                 boolean allowStoredEntriesWithDataDescriptor) {
        super(inputStream);
        zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        in = new PushbackInputStream(inputStream, buf.capacity());
        this.allowStoredEntriesWithDataDescriptor =
            allowStoredEntriesWithDataDescriptor;
        // haven't read anything so far
    }