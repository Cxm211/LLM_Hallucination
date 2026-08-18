public void finish() throws IOException {
        if (currName != null) {
            throw new IOException("This archive contains unclosed entries.");
        }
        writeEOFRecord();
        writeEOFRecord();
    }