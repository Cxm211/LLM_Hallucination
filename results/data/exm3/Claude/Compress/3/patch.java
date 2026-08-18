public void finish() throws IOException {
    if (currBytes < currSize) {
        throw new IOException("entry '" + currName + "' not closed before finishing archive");
    }
    writeEOFRecord();
    writeEOFRecord();
}