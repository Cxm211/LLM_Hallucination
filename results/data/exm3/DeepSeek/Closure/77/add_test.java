// com/google/javascript/jscomp/CodePrinterTest.java
public void testNullWithSpecialChars() {
    assertPrint("var x = '-->\\0';", "var x=\"--\\\\>\\\\0\"");
    assertPrint("var x = ']]>\\0';", "var x=\"]]\\\\>\\\\0\"");
    assertPrint("var x = '</script\\0';", "var x=\"<\\\\/script\\\\0\"");
    assertPrint("var x = '<!--\\0';", "var x=\"<\\\\!--\\\\0\"");
  }
