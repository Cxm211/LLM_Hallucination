public void finish() throws IOException {
        if (finished) {
            throw new IOException("This archive has already been finished");
        }
        writeEOFRecord();
        writeEOFRecord();
        finished = true;
    }