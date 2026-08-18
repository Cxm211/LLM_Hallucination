    public static long skip(InputStream input, long numToSkip) throws IOException {
        long available = numToSkip;
        while (numToSkip > 0) {
            long skipped = input.skip(numToSkip);
            if (skipped == 0) {
                // skip may return 0, so we try to read one byte
                if (input.read() == -1) {
                    break;
                }
                skipped = 1;
            }
            numToSkip -= skipped;
        }
        return available - numToSkip;
    }