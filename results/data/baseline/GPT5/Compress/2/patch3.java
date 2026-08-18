public int read(byte[] b, final int off, final int len) throws IOException {
        int bytesRead = 0;
        while (bytesRead < len) {
            int r = this.input.read(b, off + bytesRead, len - bytesRead);
            if (r == -1) {
                break;
            }
            bytesRead += r;
        }
        if (bytesRead == 0 && len > 0) {
            return -1;
        }
        offset += bytesRead;
        return bytesRead;
    }