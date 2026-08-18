public void finish() throws IOException {
    if (currSize > 0 || currBytes > 0) {
        throw new IOException("entry '" + currName + "' has not been closed");
    }
    writeEOFRecord();
    writeEOFRecord();
}