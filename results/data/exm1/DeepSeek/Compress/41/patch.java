public ZipArchiveEntry getNextZipEntry() throws IOException {
    boolean firstEntry = true;
    if (closed || hitCentralDirectory) {
        return null;
    }
    if (current != null) {
        closeEntry();
        firstEntry = false;
    }

    byte[] lfh = new byte[30];
    try {
        if (firstEntry) {
            readFirstLocalFileHeader(lfh);
        } else {
            readFully(lfh);
        }
    } catch (final EOFException e) {
        return null;
    }

    final ZipLong sig = new ZipLong(lfh);
    if (sig.equals(ZipLong.CFH_SIG) || sig.equals(ZipLong.AED_SIG)) {
        hitCentralDirectory = true;
        skipRemainderOfArchive();
    }
    if (!sig.equals(ZipLong.LFH_SIG)) {
        return null;
    }

    int off = WORD;
    current = new CurrentEntry();

    final int versionMadeBy = ZipShort.getValue(lfh, off);
    off += SHORT;
    current.entry.setPlatform((versionMadeBy >> ZipFile.BYTE_SHIFT) & ZipFile.NIBLET_MASK);

    final GeneralPurposeBit gpFlag = GeneralPurposeBit.parse(lfh, off);
    final boolean hasUTF8Flag = gpFlag.usesUTF8ForNames();
    final ZipEncoding entryEncoding = hasUTF8Flag ? ZipEncodingHelper.UTF8_ZIP_ENCODING : zipEncoding;
    current.hasDataDescriptor = gpFlag.usesDataDescriptor();
    current.entry.setGeneralPurposeBit(gpFlag);

    off += SHORT;

    current.entry.setMethod(ZipShort.getValue(lfh, off));
    off += SHORT;

    final long time = ZipUtil.dosToJavaTime(ZipLong.getValue(lfh, off));
    current.entry.setTime(time);
    off += WORD;

    ZipLong size = null, cSize = null;
    if (!current.hasDataDescriptor) {
        current.entry.setCrc(ZipLong.getValue(lfh, off));
        off += WORD;

        cSize = new ZipLong(lfh, off);
        off += WORD;

        size = new ZipLong(lfh, off);
        off += WORD;
    } else {
        off += 3 * WORD;
    }

    final int fileNameLen = ZipShort.getValue(lfh, off);
    off += SHORT;

    final int extraLen = ZipShort.getValue(lfh, off);
    off += SHORT;

    final byte[] fileName = new byte[fileNameLen];
    readFully(fileName);
    current.entry.setName(entryEncoding.decode(fileName), fileName);

    final byte[] extraData = new byte[extraLen];
    readFully(extraData);
    current.entry.setExtra(extraData);

    if (!hasUTF8Flag && useUnicodeExtraFields) {
        ZipUtil.setNameAndCommentFromExtraFields(current.entry, fileName, null);
    }

    processZip64Extra(size, cSize);

    if (current.entry.getCompressedSize() != ArchiveEntry.SIZE_UNKNOWN) {
        if (current.entry.getMethod() == ZipMethod.UNSHRINKING.getCode()) {
            current.in = new UnshrinkingInputStream(new BoundedInputStream(in, current.entry.getCompressedSize()));
        } else if (current.entry.getMethod() == ZipMethod.IMPLODING.getCode()) {
            current.in = new ExplodingInputStream(
                    current.entry.getGeneralPurposeBit().getSlidingDictionarySize(),
                    current.entry.getGeneralPurposeBit().getNumberOfShannonFanoTrees(),
                    new BoundedInputStream(in, current.entry.getCompressedSize()));
        } else if (current.entry.getMethod() == ZipMethod.BZIP2.getCode()) {
            current.in = new BZip2CompressorInputStream(new BoundedInputStream(in, current.entry.getCompressedSize()));
        }
    }
    
    entriesRead++;
    return current.entry;
}