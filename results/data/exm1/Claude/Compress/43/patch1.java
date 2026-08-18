private byte[] createLocalFileHeader(final ZipArchiveEntry ze, final ByteBuffer name, final boolean encodable,
                                     final boolean phased, long archiveOffset) throws IOException {
    ResourceAlignmentExtraField oldAlignmentEx =
        (ResourceAlignmentExtraField) ze.getExtraField(ResourceAlignmentExtraField.ID);
    if (oldAlignmentEx != null) {
        ze.removeExtraField(ResourceAlignmentExtraField.ID);
    }

    int alignment = ze.getAlignment();
    if (alignment <= 0 && oldAlignmentEx != null) {
        alignment = oldAlignmentEx.getAlignment();
    }

    if (alignment > 1 || (oldAlignmentEx != null && !oldAlignmentEx.allowMethodChange())) {
        int oldLength = LFH_FILENAME_OFFSET +
                        name.limit() - name.position() +
                        ze.getLocalFileDataExtra().length;

        int padding = (int) ((-archiveOffset - oldLength - ZipExtraField.EXTRAFIELD_HEADER_SIZE
                        - ResourceAlignmentExtraField.BASE_SIZE) &
                        (alignment - 1));
        ze.addExtraField(new ResourceAlignmentExtraField(alignment,
                        oldAlignmentEx != null && oldAlignmentEx.allowMethodChange(), padding));
    }

    final byte[] extra = ze.getLocalFileDataExtra();
    final int nameLen = name.limit() - name.position();
    final int len = LFH_FILENAME_OFFSET + nameLen + extra.length;
    final byte[] buf = new byte[len];

    System.arraycopy(LFH_SIG,  0, buf, LFH_SIG_OFFSET, WORD);

    final int zipMethod = ze.getMethod();
    final boolean dataDescriptor = usesDataDescriptor(zipMethod);

    putShort(versionNeededToExtract(zipMethod, hasZip64Extra(ze), dataDescriptor), buf, LFH_VERSION_NEEDED_OFFSET);

    final GeneralPurposeBit generalPurposeBit = getGeneralPurposeBits(!encodable && fallbackToUTF8, dataDescriptor);
    generalPurposeBit.encode(buf, LFH_GPB_OFFSET);

    putShort(zipMethod, buf, LFH_METHOD_OFFSET);

    ZipUtil.toDosTime(calendarInstance, ze.getTime(), buf, LFH_TIME_OFFSET);

    if (phased){
        putLong(ze.getCrc(), buf, LFH_CRC_OFFSET);
    } else if (zipMethod == DEFLATED || channel != null) {
        System.arraycopy(LZERO, 0, buf, LFH_CRC_OFFSET, WORD);
    } else {
        putLong(ze.getCrc(), buf, LFH_CRC_OFFSET);
    }

    if (hasZip64Extra(ze)){
        ZipLong.ZIP64_MAGIC.putLong(buf, LFH_COMPRESSED_SIZE_OFFSET);
        ZipLong.ZIP64_MAGIC.putLong(buf, LFH_ORIGINAL_SIZE_OFFSET);
    } else if (phased) {
        putLong(ze.getCompressedSize(), buf, LFH_COMPRESSED_SIZE_OFFSET);
        putLong(ze.getSize(), buf, LFH_ORIGINAL_SIZE_OFFSET);
    } else if (zipMethod == DEFLATED || channel != null) {
        System.arraycopy(LZERO, 0, buf, LFH_COMPRESSED_SIZE_OFFSET, WORD);
        System.arraycopy(LZERO, 0, buf, LFH_ORIGINAL_SIZE_OFFSET, WORD);
    } else {
        putLong(ze.getSize(), buf, LFH_COMPRESSED_SIZE_OFFSET);
        putLong(ze.getSize(), buf, LFH_ORIGINAL_SIZE_OFFSET);
    }
    putShort(nameLen, buf, LFH_FILENAME_LENGTH_OFFSET);

    putShort(extra.length, buf, LFH_EXTRA_LENGTH_OFFSET);

    System.arraycopy( name.array(), name.arrayOffset(), buf, LFH_FILENAME_OFFSET, nameLen);

    System.arraycopy(extra, 0, buf, LFH_FILENAME_OFFSET + nameLen, extra.length);

    return buf;
}