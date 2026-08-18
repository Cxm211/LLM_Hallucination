public void close() throws IOException {
        if (!this.closed) {
            IOException prior = null;
            try {
                this.finish();
            } catch (IOException e) {
                prior = e;
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ioe) {
                    if (prior == null) {
                        prior = ioe;
                    }
                }
                this.closed = true;
            }
            if (prior != null) {
                throw prior;
            }
        }
    }