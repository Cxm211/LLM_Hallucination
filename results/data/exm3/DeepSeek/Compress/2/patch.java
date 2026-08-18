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

        // skip padding byte if we are at an odd offset
        if (offset % 2 != 0) {
            if (read() < 0) {
                return null; // EOF while skipping padding
            }
        }

        final byte[] header = new byte[60];
        final int read = read(header, 0, 60);
        if (read == -1) {
            // reached EOF before any data
            return null;
        }
        if (read != 60) {
            throw new IOException("failed to read entry header");
        }

        final byte[] name = new byte[16];
        final byte[] lastmodified = new byte[12];
        final byte[] userid = new byte[6];
        final byte[] groupid = new byte[6];
        final byte[] filemode = new byte[8];
        final byte[] length = new byte[10];
        final byte[] trailer = new byte[2];
        System.arraycopy(header, 0, name, 0, 16);
        System.arraycopy(header, 16, lastmodified, 0, 12);
        System.arraycopy(header, 28, userid, 0, 6);
        System.arraycopy(header, 34, groupid, 0, 6);
        System.arraycopy(header, 40, filemode, 0, 8);
        System.arraycopy(header, 48, length, 0, 10);
        System.arraycopy(header, 58, trailer, 0, 2);

        {
            final byte[] expected = ArArchiveEntry.TRAILER.getBytes();
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != trailer[i]) {
                    throw new IOException("invalid entry header. not read the content?");
                }
            }
        }

        return new ArArchiveEntry(new String(name).trim(),
                                          Long.parseLong(new String(length)
                                                         .trim()));
    }