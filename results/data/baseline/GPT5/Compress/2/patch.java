public ArArchiveEntry getNextArEntry() throws IOException {
                    // hit EOF before previous entry was complete
                    // TODO: throw an exception instead?

        if (offset == 0) {
            final byte[] expected = ArArchiveEntry.HEADER.getBytes();
            final byte[] realized = new byte[expected.length]; 
            final int read = read(realized);
            if (read != expected.length) {
                throw new IOException("failed to read header");
            }
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != realized[i]) {
                    throw new IOException("invalid header " + new String(realized));
                }
            }
        }

                // hit eof

        if (offset % 2 != 0) {
            read();
        }
        final byte[] name = new byte[16];
        final byte[] lastmodified = new byte[12];
        final byte[] userid = new byte[6];
        final byte[] groupid = new byte[6];
        final byte[] filemode = new byte[8];
        final byte[] length = new byte[10];

        int first = read();
        if (first == -1) {
            return null;
        }
        name[0] = (byte) first;
        if (read(name, 1, name.length - 1) != name.length - 1) {
            throw new IOException("failed to read entry name");
        }
        if (read(lastmodified, 0, lastmodified.length) != lastmodified.length) {
            throw new IOException("failed to read entry lastmodified");
        }
        if (read(userid, 0, userid.length) != userid.length) {
            throw new IOException("failed to read entry userid");
        }
        if (read(groupid, 0, groupid.length) != groupid.length) {
            throw new IOException("failed to read entry groupid");
        }
        if (read(filemode, 0, filemode.length) != filemode.length) {
            throw new IOException("failed to read entry filemode");
        }
        if (read(length, 0, length.length) != length.length) {
            throw new IOException("failed to read entry length");
        }

        {
            final byte[] expected = ArArchiveEntry.TRAILER.getBytes();
            final byte[] realized = new byte[expected.length]; 
            final int read = read(realized);
            if (read != expected.length) {
                throw new IOException("failed to read entry header");
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