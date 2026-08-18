public void close() throws IOException {
        if (!this.closed) {
            this.finish();
            if (this.out != null) {
                this.out.close();
            }
            this.closed = true;
        }
    }