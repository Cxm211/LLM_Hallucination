public void finish() throws IOException {
    if (currName != null) {
        throw new IOException("there are open entries");
    }
    writeEOFRecord();
    writeEOFRecord();
}