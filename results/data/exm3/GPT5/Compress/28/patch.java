public int read(byte[] buf, int offset, int numToRead) throws IOException {
    int totalRead = 0;

    if (hasHitEOF || entryOffset >= entrySize) {
        return -1;
    }

    if (currEntry == null) {
        throw new IllegalStateException("No current tar entry");
    }

    numToRead = Math.min(numToRead, available());

    totalRead = is.read(buf, offset, numToRead);

    if (totalRead == -1) {
        // Unexpected EOF before the end of the current entry -> truncated archive
        hasHitEOF = true;
        if (entryOffset < entrySize) {
            throw new IOException("Truncated TAR archive");
        }
        return -1;
    } else {
        count(totalRead);
        entryOffset += totalRead;
        return totalRead;
    }
}