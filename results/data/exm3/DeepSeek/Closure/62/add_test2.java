// com/google/javascript/jscomp/LightweightMessageFormatterTest.java
public void testFormatErrorSpaceEndOfLineEmpty() throws Exception {
    JSError error = JSError.make("javascript/complex.js",
        1, 0, FOO_TYPE);
    LightweightMessageFormatter formatter = formatter("");
    assertEquals("javascript/complex.js:1: ERROR - error description here\n" +
        "\n" +
        "^\n", formatter.formatError(error));
  }
