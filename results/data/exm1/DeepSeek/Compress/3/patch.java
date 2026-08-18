public void finish() throws IOException {
    if (currName != null) {
        throw new IOException("Entry remains open: " + currName);
    }
    writeEOFRecord();
    writeEOFRecord();
}