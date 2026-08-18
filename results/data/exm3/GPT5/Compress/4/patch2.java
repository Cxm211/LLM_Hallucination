public void close() throws IOException {
        IOException prior = null;
        try {
            finish();
        } catch (IOException e) {
            prior = e;
        } finally {
            try {
                if (raf != null) {
                    raf.close();
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
        }
        if (prior != null) {
            throw prior;
        }
    }