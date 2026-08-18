    public int read(byte[] b, final int off, final int len) throws IOException {
        if (remaining <= 0) {
            return -1;
        }
        int toRead = (int) Math.min(len, remaining);
        final int ret = this.input.read(b, off, toRead);
        if (ret > 0) {
            offset += ret;
            remaining -= ret;
        }
        return ret;
    }