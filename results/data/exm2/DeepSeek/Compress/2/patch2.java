    public int read() throws IOException {
        if (remaining <= 0) {
            return -1;
        }
        final int ret = input.read();
        if (ret > 0) {
            offset += 1;
            remaining -= 1;
        }
        return ret;
    }