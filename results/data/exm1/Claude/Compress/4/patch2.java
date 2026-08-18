public void close() throws IOException {
        try {
            finish();
        } finally {
            if (raf != null) {
                raf.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }