public ArArchiveEntry getNextArEntry() throws IOException {
    if (offset == 0) {
        final byte[] expected = ArArchiveEntry.HEADER.getBytes();
        final byte[] realized = new byte[expected.length];
        int off = 0;
        while (off < expected.length) {
            int ret = read(realized, off, expected.length - off);
            if (ret == -1) {
                throw new IOException("failed to read header");
            }
            off += ret;
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != realized[i]) {
                throw new IOException("invalid header " + new String(realized));
            }
        }
    }

    // Handle parity padding
    if (offset % 2 != 0) {
        int pad = read();
        if (pad == -1) {
            return null;
        }
    }

    // Read name
    final byte[] name = new byte[16];
    int off = 0;
    while (off < 16) {
        int ret = read(name, off, 16 - off);
        if (ret == -1) {
            return null;
        }
        off += ret;
    }

    // Read lastmodified
    final byte[] lastmodified = new byte[12];
    off = 0;
    while (off < 12) {
        int ret = read(lastmodified, off, 12 - off);
        if (ret == -1) {
            throw new IOException("unexpected EOF while reading entry");
        }
        off += ret;
    }

    // Read userid
    final byte[] userid = new byte[6];
    off = 0;
    while (off < 6) {
        int ret = read(userid, off, 6 - off);
        if (ret == -1) {
            throw new IOException("unexpected EOF while reading entry");
        }
        off += ret;
    }

    // Read groupid
    final byte[] groupid = new byte[6];
    off = 0;
    while (off < 6) {
        int ret = read(groupid, off, 6 - off);
        if (ret == -1) {
            throw new IOException("unexpected EOF while reading entry");
        }
        off += ret;
    }

    // Read filemode
    final byte[] filemode = new byte[8];
    off = 0;
    while (off < 8) {
        int ret = read(filemode, off, 8 - off);
        if (ret == -1) {
            throw new IOException("unexpected EOF while reading entry");
        }
        off += ret;
    }

    // Read length
    final byte[] length = new byte[10];
    off = 0;
    while (off < 10) {
        int ret = read(length, off, 10 - off);
        if (ret == -1) {
            throw new IOException("unexpected EOF while reading entry");
        }
        off += ret;
    }

    // Read trailer
    final byte[] expectedTrailer = ArArchiveEntry.TRAILER.getBytes();
    final byte[] realizedTrailer = new byte[expectedTrailer.length];
    off = 0;
    while (off < expectedTrailer.length) {
        int ret = read(realizedTrailer, off, expectedTrailer.length - off);
        if (ret == -1) {
            throw new IOException("failed to read entry trailer");
        }
        off += ret;
    }
    for (int i = 0; i < expectedTrailer.length; i++) {
        if (expectedTrailer[i] != realizedTrailer[i]) {
            throw new IOException("invalid entry trailer");
        }
    }

    return new ArArchiveEntry(new String(name).trim(),
                              Long.parseLong(new String(length).trim()));
}