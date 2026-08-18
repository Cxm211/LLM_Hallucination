public DumpArchiveInputStream(InputStream is, String encoding) throws ArchiveException {
    this.raw = new TapeInputStream(is);
    this.hasHitEOF = false;
    this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
    try {
        byte[] headerBytes = raw.readRecord();
        if (!DumpArchiveUtil.verify(headerBytes)) {
            throw new UnrecognizedFormatException();
        }
        summary = new DumpArchiveSummary(headerBytes, this.zipEncoding);
        raw.resetBlockSize(summary.getNTRec(), summary.isCompressed());
        blockBuffer = new byte[4 * DumpArchiveConstants.TP_SIZE];
        readCLRI();
        readBITS();
    } catch (IOException ex) {
        throw new ArchiveException(ex.getMessage(), ex);
    }
    Dirent root = new Dirent(2, 2, 4, ".");
    names.put(2, root);
    queue = new PriorityQueue<DumpArchiveEntry>(10,
            new Comparator<DumpArchiveEntry>() {
                public int compare(DumpArchiveEntry p, DumpArchiveEntry q) {
                    if (p.getOriginalName() == null || q.getOriginalName() == null) {
                        return Integer.MAX_VALUE;
                    }
                    return p.getOriginalName().compareTo(q.getOriginalName());
                }
            });
}