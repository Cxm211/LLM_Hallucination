    public TarArchiveEntry getNextTarEntry() throws IOException {
        if (hasHitEOF) {
            return null;
        }

        if (currEntry != null) {
            long numToSkip = entrySize - entryOffset;

            while (numToSkip > 0) {
                long skipped = skip(numToSkip);
                if (skipped <= 0) {
                    throw new RuntimeException("failed to skip current tar entry");
                }
                numToSkip -= skipped;
            }

            readBuf = null;
        }

        byte[] headerBuf = getRecord();

        if (hasHitEOF) {
            currEntry = null;
            return null;
        }

        currEntry = new TarArchiveEntry(headerBuf);
        entryOffset = 0;
        entrySize = currEntry.getSize();

        if (currEntry.isGNULongNameEntry()) {
            // read in the name
            byte[] nameBytes = new byte[(int) entrySize];
            int totalRead = 0;
            while (totalRead < entrySize) {
                int read = read(nameBytes, totalRead, (int) (entrySize - totalRead));
                if (read == -1) {
                    throw new IOException("Truncated tar file");
                }
                totalRead += read;
            }
            // remove trailing null terminator
            int len = entrySize;
            while (len > 0 && nameBytes[len - 1] == 0) {
                len--;
            }
            String longName = new String(nameBytes, 0, len, "UTF-8");
            getNextEntry();
            if (currEntry == null) {
                // Bugzilla: 40334
                // Malformed tar file - long entry name not followed by entry
                return null;
            }
            currEntry.setName(longName);
        }

        if (currEntry.isPaxHeader()){ // Process Pax headers
            paxHeaders();
        }

        if (currEntry.isGNUSparse()){ // Process sparse files
            readGNUSparse();
        }

        // If the size of the next element in the archive has changed
        // due to a new size being reported in the posix header
        // information, we update entrySize here so that it contains
        // the correct value.
        entrySize = currEntry.getSize();
        return currEntry;
    }