public static long skip(InputStream input, long numToSkip) throws IOException {
    long available = numToSkip;
    while (numToSkip > 0) {
        long skipped = input.skip(numToSkip);
        if (skipped == 0) {
            int readByte = input.read();
            if (readByte == -1) {
                break;
            }
            numToSkip--;
        } else {
            numToSkip -= skipped;
        }
    }
        
    return available - numToSkip;
}