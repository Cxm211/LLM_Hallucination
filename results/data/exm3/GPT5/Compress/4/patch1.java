public void close() throws IOException {
        if (!closed) {
            IOException prior = null;
            try {
                finish();
            } catch (IOException e) {
                prior = e;
            } finally {
                try {
                    if (buffer != null) {
                        buffer.close();
                    }
                } catch (IOException ioe) {
                    if (prior == null) {
                        prior = ioe;
                    }
                }
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ioe) {
                    if (prior == null) {
                        prior = ioe;
                    }
                }
                closed = true;
            }
            if (prior != null) {
                throw prior;
            }
        }
    }