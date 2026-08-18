public void close() throws IOException {
    try {
        finish();
    } finally {
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                // Suppress exception from raf.close()
            }
        }
        if (out != null) {
            out.close();
        }
    }
}