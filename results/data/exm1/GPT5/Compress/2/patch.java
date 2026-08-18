public ArArchiveEntry getNextArEntry() throws IOException {
                    // hit EOF before previous entry was complete
                    // TODO: throw an exception instead?

        if (offset == 0) {
            final byte[] expected = ArArchiveEntry.HEADER.getBytes();
            final byte[] realized = new byte[expected.length];
            int filled = 0;
            while (filled < realized.length) {
                int r = read(realized, filled, realized.length - filled);
                if (r == -1) {
                    throw new IOException("failed to read header");
                }
                filled += r;
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

        // Reliable EOF detection: try to read the first byte of the next header's name
        final int first = input.read();
        if (first == -1) {
            return null;
        }
        offset += 1;

        final byte[] name = new byte[16];
        name[0] = (byte) first;
        int need = 15;
        int filled = 1;
        while (need > 0) {
            int r = read(name, filled, need);
            if (r == -1) {
                throw new IOException("failed to read entry name");
            }
            filled += r;
            need -= r;
        }

        final byte[] lastmodified = new byte[12];
        filled = 0;
        while (filled < lastmodified.length) {
            int r = read(lastmodified, filled, lastmodified.length - filled);
            if (r == -1) {
                throw new IOException("failed to read lastmodified");
            }
            filled += r;
        }

        final byte[] userid = new byte[6];
        filled = 0;
        while (filled < userid.length) {
            int r = read(userid, filled, userid.length - filled);
            if (r == -1) {
                throw new IOException("failed to read userid");
            }
            filled += r;
        }

        final byte[] groupid = new byte[6];
        filled = 0;
        while (filled < groupid.length) {
            int r = read(groupid, filled, groupid.length - filled);
            if (r == -1) {
                throw new IOException("failed to read groupid");
            }
            filled += r;
        }

        final byte[] filemode = new byte[8];
        filled = 0;
        while (filled < filemode.length) {
            int r = read(filemode, filled, filemode.length - filled);
            if (r == -1) {
                throw new IOException("failed to read filemode");
            }
            filled += r;
        }

        final byte[] length = new byte[10];
        filled = 0;
        while (filled < length.length) {
            int r = read(length, filled, length.length - filled);
            if (r == -1) {
                throw new IOException("failed to read length");
            }
            filled += r;
        }

        {
            final byte[] expected = ArArchiveEntry.TRAILER.getBytes();
            final byte[] realized = new byte[expected.length];
            filled = 0;
            while (filled < realized.length) {
                int r = read(realized, filled, realized.length - filled);
                if (r == -1) {
                    throw new IOException("failed to read entry header");
                }
                filled += r;
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