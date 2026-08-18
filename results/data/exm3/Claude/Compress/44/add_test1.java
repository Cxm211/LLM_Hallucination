// org/apache/commons/compress/utils/ChecksumCalculatingInputStreamTest.java
@Test(expected = NullPointerException.class)
public void testNullChecksumThrowsBeforeNullInputStream() {
    new ChecksumCalculatingInputStream(null, null);
}