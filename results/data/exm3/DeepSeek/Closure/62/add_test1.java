// com/google/javascript/jscomp/LightweightMessageFormatterTest.java
public void testFormatErrorSpaceEndOfLineWithSpaces() throws Exception {
    JSError error = JSError.make("javascript/complex.js",
        1, 5, FOO_TYPE);
    LightweightMessageFormatter formatter = formatter("  a  ");
    assertEquals("javascript/complex.js:1: ERROR - error description here\n" +
        "  a  \n" +
        "     ^\n", formatter.formatError(error));
  }
