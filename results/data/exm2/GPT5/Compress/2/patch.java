public ArArchiveEntry getNextArEntry() throws IOException {
    // hit EOF before previous entry was complete
    // TODO: throw an exception instead?

    if (offset == 0) {
        final byte[] expected = ArArchiveEntry.HEADER.getBytes();
        final byte[] realized = new byte[expected.length];
        int readTotal = 0;
        while (readTotal < realized.length) {
            int r = read(realized, readTotal, realized.length - readTotal);
            if (r == -1) {
                throw new IOException("failed to read header");
            }
            readTotal += r;
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != realized[i]) {
                throw new IOException("invalid header " + new String(realized));
            }
        }
    }

    // hit eof

    if (input.available() == 0) {
        return null;
    }

    if (offset % 2 != 0) {
        read();
    }
    final byte[] name = new byte[16];
    final byte[] lastmodified = new byte[12];
    final byte[] userid = new byte[6];
    final byte[] groupid = new byte[6];
    final byte[] filemode = new byte[8];
    final byte[] length = new byte[10];

    int n;
    n = 0; while (n < name.length) { int r = read(name, n, name.length - n); if (r == -1) throw new IOException("failed to read entry header"); n += r; }
    n = 0; while (n < lastmodified.length) { int r = read(lastmodified, n, lastmodified.length - n); if (r == -1) throw new IOException("failed to read entry header"); n += r; }
    n = 0; while (n < userid.length) { int r = read(userid, n, userid.length - n); if (r == -1) throw new IOException("failed to read entry header"); n += r; }
    n = 0; while (n < groupid.length) { int r = read(groupid, n, groupid.length - n); if (r == -1) throw new IOException("failed to read entry header"); n += r; }
    n = 0; while (n < filemode.length) { int r = read(filemode, n, filemode.length - n); if (r == -1) throw new IOException("failed to read entry header"); n += r; }
    n = 0; while (n < length.length) { int r = read(length, n, length.length - n); if (r == -1) throw new IOException("failed to read entry header"); n += r; }

    {
        final byte[] expected = ArArchiveEntry.TRAILER.getBytes();
        final byte[] realized = new byte[expected.length];
        int readTotal2 = 0;
        while (readTotal2 < realized.length) {
            int r = read(realized, readTotal2, realized.length - readTotal2);
            if (r == -1) {
                throw new IOException("failed to read entry header");
            }
            readTotal2 += r;
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != realized[i]) {
                throw new IOException("invalid entry header. not read the content?");
            }
        }
    }

    return new ArArchiveEntry(new String(name).trim(),
                                      Long.parseLong(new String(length)
                                                     .trim()));
}