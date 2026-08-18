public ArArchiveEntry getNextArEntry() throws IOException {
        // read global header once at start
        if (offset == 0) {
            final byte[] expected = ArArchiveEntry.HEADER.getBytes();
            final byte[] realized = new byte[expected.length];
            int done = 0;
            while (done < realized.length) {
                int r = read(realized, done, realized.length - done);
                if (r == -1) {
                    throw new IOException("failed to read header");
                }
                done += r;
            }
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != realized[i]) {
                    throw new IOException("invalid header " + new String(realized));
                }
            }
        }

        // align to even boundary between entries
        if (offset % 2 != 0) {
            int skipped = read();
            if (skipped == -1) {
                return null;
            }
        }

        // Try to read the first byte of the name to detect EOF reliably
        int first = read();
        if (first == -1) {
            return null; // proper EOF
        }

        final byte[] name = new byte[16];
        final byte[] lastmodified = new byte[12];
        final byte[] userid = new byte[6];
        final byte[] groupid = new byte[6];
        final byte[] filemode = new byte[8];
        final byte[] length = new byte[10];

        name[0] = (byte) first;
        // fill the rest of the fixed-size header fields fully
        int done;
        done = 1;
        while (done < name.length) {
            int r = read(name, done, name.length - done);
            if (r == -1) {
                throw new IOException("failed to read entry name");
            }
            done += r;
        }

        done = 0;
        while (done < lastmodified.length) {
            int r = read(lastmodified, done, lastmodified.length - done);
            if (r == -1) {
                throw new IOException("failed to read entry header");
            }
            done += r;
        }

        done = 0;
        while (done < userid.length) {
            int r = read(userid, done, userid.length - done);
            if (r == -1) {
                throw new IOException("failed to read entry header");
            }
            done += r;
        }

        done = 0;
        while (done < groupid.length) {
            int r = read(groupid, done, groupid.length - done);
            if (r == -1) {
                throw new IOException("failed to read entry header");
            }
            done += r;
        }

        done = 0;
        while (done < filemode.length) {
            int r = read(filemode, done, filemode.length - done);
            if (r == -1) {
                throw new IOException("failed to read entry header");
            }
            done += r;
        }

        done = 0;
        while (done < length.length) {
            int r = read(length, done, length.length - done);
            if (r == -1) {
                throw new IOException("failed to read entry header");
            }
            done += r;
        }

        {
            final byte[] expected = ArArchiveEntry.TRAILER.getBytes();
            final byte[] realized = new byte[expected.length];
            done = 0;
            while (done < realized.length) {
                int r = read(realized, done, realized.length - done);
                if (r == -1) {
                    throw new IOException("failed to read entry header");
                }
                done += r;
            }
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != realized[i]) {
                    throw new IOException("invalid entry header. not read the content?");
                }
            }
        }

        return new ArArchiveEntry(new String(name).trim(),
                                  Long.parseLong(new String(length).trim()));
    }