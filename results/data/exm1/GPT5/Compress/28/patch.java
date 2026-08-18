public int read(byte[] buf, int offset, int numToRead) throws IOException {
    int totalRead = 0;

    if (hasHitEOF || entryOffset >= entrySize) {
        return -1;
    }

    if (currEntry == null) {
        throw new IllegalStateException("No current tar entry");
    }

    numToRead = Math.min(numToRead, available());
    if (numToRead == 0) {
        return 0;
    }

    totalRead = is.read(buf, offset, numToRead);

    if (totalRead == -1) {
        hasHitEOF = true;
        throw new IOException("Truncated TAR archive");
    }

    count(totalRead);
    entryOffset += totalRead;

    return totalRead;
}
