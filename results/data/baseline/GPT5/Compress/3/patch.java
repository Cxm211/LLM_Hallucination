public void finish() throws IOException {
        closeArchiveEntry();
        writeEOFRecord();
        writeEOFRecord();
    }