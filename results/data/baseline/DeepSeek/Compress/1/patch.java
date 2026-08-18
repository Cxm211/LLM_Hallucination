    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            super.close();
        }
    }