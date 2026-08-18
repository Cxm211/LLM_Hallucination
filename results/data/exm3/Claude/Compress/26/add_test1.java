// org/apache/commons/compress/utils/IOUtilsTest.java
@Test
public void skipWithMixedSkipAndReadBehavior() throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
    FilterInputStream fis = new FilterInputStream(bais) {
        private int callCount = 0;
        public long skip(long s) throws IOException {
            callCount++;
            if (callCount == 1) {
                return super.skip(Math.min(s, 3));
            }
            return 0;
        }
    };
    long skipped = IOUtils.skip(fis, 8);
    assertEquals(8, skipped);
}