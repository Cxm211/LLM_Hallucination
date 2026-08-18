// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
@Test
pubic void testParseOctalSingleDigitWithTrailing() throws Exception {
    byte[] buffer = " 7\0 ".getBytes(CharsetNames.UTF_8); // Single octal digit
    long result = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(7L, result);
}