private void writeLocalFileHeader(final ZipArchiveEntry ze, final boolean phased) throws IOException {
        final boolean encodable = zipEncoding.canEncode(ze.getName());
        final ByteBuffer name = getName(ze);

        if (createUnicodeExtraFields != UnicodeExtraFieldPolicy.NEVER) {
            addUnicodeExtraFields(ze, encodable, name);
        }

        final long localHeaderStart = streamCompressor.getTotalBytesWritten();
        final byte[] localHeader = createLocalFileHeader(ze, name, encodable, phased, localHeaderStart);
        final boolean dataDescriptor = !phased && usesDataDescriptor(ze.getMethod());
        metaData.put(ze, new EntryMetaData(localHeaderStart, dataDescriptor));
        entry.localDataStart = localHeaderStart + LFH_CRC_OFFSET; // At crc offset
        writeCounted(localHeader);
        entry.dataStart = streamCompressor.getTotalBytesWritten();
    }