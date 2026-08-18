// org/apache/commons/compress/utils/IOUtilsTest.java
@Test
public void skipWithEndOfStream() throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3});
    FilterInputStream fis = new FilterInputStream(bais) {
        public long skip(long s) {
            return 0;
        }
    };
    long skipped = IOUtils.skip(fis, 10);
    assertEquals(3, skipped);
}