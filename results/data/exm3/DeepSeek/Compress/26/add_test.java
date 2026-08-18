// org/apache/commons/compress/utils/IOUtilsTest.java
@Test
    public void skipAlternatingSkipAndRead() throws Exception {
        skip(new StreamWrapper() {
                public InputStream wrap(final InputStream toWrap) {
                    return new FilterInputStream(toWrap) {
                        int callCount = 0;
                        public long skip(long s) throws IOException {
                            callCount++;
                            if (callCount == 1) {
                                return 0;
                            } else if (callCount == 2) {
                                toWrap.skip(5);
                                return 5;
                            } else {
                                return 0;
                            }
                        }
                    };
                }
            });
    }
