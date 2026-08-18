// com/google/javascript/jscomp/CodePrinterTest.java
public void testControlCharactersEscaping() {
    assertPrint("var x ='\\x01';", "var x=\"\\u0001\"");
    assertPrint("var x ='\\x1f';", "var x=\"\\u001f\"");
  }