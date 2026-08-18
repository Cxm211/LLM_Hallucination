// org/apache/commons/compress/utils/IOUtilsTest.java
@Test
public void skipExactlyAvailableBytes() throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
    FilterInputStream fis = new FilterInputStream(bais) {
        public long skip(long s) {
            return 0;
        }
    };
    long skipped = IOUtils.skip(fis, 5);
    assertEquals(5, skipped);
}