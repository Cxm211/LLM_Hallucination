// com/google/javascript/jscomp/LightweightMessageFormatterTest.java
public void testFormatErrorSpaceEndOfLineZeroLineNumber() throws Exception {
    JSError error = JSError.make("javascript/complex.js",
        0, 3, FOO_TYPE);
    LightweightMessageFormatter formatter = formatter("foo");
    assertEquals("javascript/complex.js: ERROR - error description here\n" +
        "foo\n" +
        "   ^\n", formatter.formatError(error));
  }
