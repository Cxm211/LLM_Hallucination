public void finish() throws IOException {
    if (currName != null) {
        throw new IOException("This archives contains unclosed entries.");
    }
    writeEOFRecord();
    writeEOFRecord();
}