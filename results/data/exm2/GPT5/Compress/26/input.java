    public static long skip(InputStream input, long numToSkip) throws IOException {
        long available = numToSkip;
        while (numToSkip > 0) {
            long skipped = input.skip(numToSkip);
            if (skipped == 0) {
                break;
            }
            numToSkip -= skipped;
        }
            
        return available - numToSkip;
    }

// trigger testcase
@Test
    public void skipUsingRead() throws Exception {
        skip(new StreamWrapper() {
                public InputStream wrap(InputStream toWrap) {
                    return new FilterInputStream(toWrap) {
                        public long skip(long s) {
                            return 0;
                        }
                    };
                }
            });
    }

@Test
    public void skipUsingSkipAndRead() throws Exception {
        skip(new StreamWrapper() {
                public InputStream wrap(final InputStream toWrap) {
                    return new FilterInputStream(toWrap) {
                        boolean skipped;
                        public long skip(long s) throws IOException {
                            if (!skipped) {
                                toWrap.skip(5);
                                skipped = true;
                                return 5;
                            }
                            return 0;
                        }
                    };
                }
            });
    }
