public void close() throws IOException {
        if (!closed) {
            finish();
            if (raf != null) {
                raf.close();
            }
            if (out != null) {
                out.close();
            }
            closed = true;
        }
    }