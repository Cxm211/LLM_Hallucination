public static long skip(InputStream input, long numToSkip) throws IOException {
        if (numToSkip <= 0) {
            return 0L;
        }
        long remaining = numToSkip;
        while (remaining > 0) {
            long skipped = input.skip(remaining);
            if (skipped > 0) {
                remaining -= skipped;
                continue;
            }
            // skip returned 0, try to read one byte to make progress
            int b = input.read();
            if (b == -1) {
                break; // EOF
            }
            remaining--;
        }
        return numToSkip - remaining;
    }