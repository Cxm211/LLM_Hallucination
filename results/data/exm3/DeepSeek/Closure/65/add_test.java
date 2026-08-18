// com/google/javascript/jscomp/CodePrinterTest.java
public void testZeroAdditional() {
    assertPrint("var x = '\\0';", "var x=\"\\000\"");
    assertPrint("var y = 'a\\0b';", "var y=\"a\\000b\"");
    assertPrint("var z = '\\0\\n';", "var z=\"\\000\\n\"");
    assertPrint("var w = '\\0>';", "var w=\"\\000>\"");
    assertPrint("var v = '<\\0';", "var v=\"<\\000\"");
  }
