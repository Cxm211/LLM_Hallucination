    public void close() throws IOException {
        if (!this.closed) {
            this.finish();
            if (out != null) out.close();
            this.closed = true;
        }
    }