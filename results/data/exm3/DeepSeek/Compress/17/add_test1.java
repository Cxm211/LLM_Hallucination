// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test(expected = IllegalArgumentException.class)
public void testParseOctalLeadingNullWithDigits() {
    byte[] buffer = {0, '1', '2', '3', ' '};
    TarUtils.parseOctal(buffer, 0, 5);
}
