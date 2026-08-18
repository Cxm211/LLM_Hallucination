    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            try {
                this.finish();
            } finally {
                out.close();
            }
        }
    }