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
        hasHitEOF = true;
        if (entryOffset < entrySize) {
            throw new IOException("Truncated tar archive entry");
        }
        return -1;
    }

    count(totalRead);
    entryOffset += totalRead;

    return totalRead;
}