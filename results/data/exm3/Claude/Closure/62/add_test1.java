// com/google/javascript/jscomp/LightweightMessageFormatterTest.java
public void testFormatErrorBeyondEndOfLine() throws Exception {
  JSError error = JSError.make("javascript/complex.js",
      1, 15, FOO_TYPE);
  LightweightMessageFormatter formatter = formatter("assert (1;");
  assertEquals("javascript/complex.js:1: ERROR - error description here\n" +
      "assert (1;\n", formatter.formatError(error));
}