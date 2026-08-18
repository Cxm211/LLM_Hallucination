public void close() throws IOException {
        if (!closed) {
            finish();
            if (buffer != null) {
                buffer.close();
            }
            if (out != null) {
                out.close();
            }
            closed = true;
        }
    }