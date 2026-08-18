    public void finish() throws IOException {
        if (currName != null) {
            throw new IOException("there are unclosed entries prior to finishing the archive");
        }
        writeEOFRecord();
        writeEOFRecord();
    }