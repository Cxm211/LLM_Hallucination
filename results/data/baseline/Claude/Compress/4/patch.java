public void close() throws IOException {
        if (!this.closed) {
            try {
                this.finish();
            } finally {
                out.close();
                this.closed = true;
            }
        }
    }