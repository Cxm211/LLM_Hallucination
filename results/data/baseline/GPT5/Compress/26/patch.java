public static long skip(InputStream input, long numToSkip) throws IOException {
        if (numToSkip <= 0) {
            return 0;
        }
        long remaining = numToSkip;
        while (remaining > 0) {
            long skipped = input.skip(remaining);
            if (skipped > 0) {
                remaining -= skipped;
                continue;
            }
            int b = input.read();
            if (b == -1) {
                break;
            }
            remaining--;
        }
        return numToSkip - remaining;
    }