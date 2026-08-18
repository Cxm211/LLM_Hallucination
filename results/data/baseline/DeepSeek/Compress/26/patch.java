    public static long skip(InputStream input, long numToSkip) throws IOException {
        long available = numToSkip;
        while (numToSkip > 0) {
            long skipped = input.skip(numToSkip);
            if (skipped == 0) {
                if (input.read() == -1) {
                    break;
                } else {
                    numToSkip--;
                    continue;
                }
            }
            numToSkip -= skipped;
        }
            
        return available - numToSkip;
    }