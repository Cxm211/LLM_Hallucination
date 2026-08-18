// com/google/javascript/jscomp/CodePrinterTest.java
public void testDelAndSpecialChars() {
    assertPrint("var x ='\\x7f>';", "var x=\"\\u007f>\"");
    assertPrint("var x ='\\x7f-->';", "var x=\"\\u007f--\\>\"");
    assertPrint("var x ='\\x7f]]>';", "var x=\"\\u007f]]\\>\"");
    assertPrint("var x ='\\x7f</script>';", "var x=\"\\u007f<\\/script>\"");
    assertPrint("var x ='\\x7f<!--';", "var x=\"\\u007f<\\!--\"");
    assertPrint("var x ='\\x7f<';", "var x=\"\\u007f<\"");
  }
