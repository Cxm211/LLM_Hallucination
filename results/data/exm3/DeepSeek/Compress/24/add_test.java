// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalNoTrailingSpace() throws Exception {
    byte[] buffer;
    long value;
    // Test two-digit octal without trailing space
    buffer = "12".getBytes(CharsetNames.UTF_8);
    value = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(10L, value); // 12 octal = 10 decimal
    // Test longer octal without trailing space
    buffer = "1234567".getBytes(CharsetNames.UTF_8);
    value = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(01234567L, value); // octal literal
    // Test with leading spaces and no trailing space
    buffer = "   777".getBytes(CharsetNames.UTF_8);
    value = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(0777L, value); // 777 octal = 511 decimal
    // Test 11-digit octal without trailing space
    buffer = "77777777777".getBytes(CharsetNames.UTF_8); // 11 sevens
    value = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(077777777777L, value); // 11-digit max
}
