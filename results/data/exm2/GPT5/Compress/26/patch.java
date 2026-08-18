public static long skip(InputStream input, long numToSkip) throws IOException {
        long available = numToSkip;
        while (numToSkip > 0) {
            long skipped = input.skip(numToSkip);
            if (skipped == 0) {
                int b = input.read();
                if (b == -1) {
                    break;
                }
                numToSkip--;
                continue;
            }
            numToSkip -= skipped;
        }
        return available - numToSkip;
    }