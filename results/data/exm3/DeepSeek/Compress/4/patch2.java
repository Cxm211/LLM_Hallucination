    public void close() throws IOException {
        if (raf != null || out != null) {
            finish();
            if (raf != null) {
                raf.close();
                raf = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
        }
    }