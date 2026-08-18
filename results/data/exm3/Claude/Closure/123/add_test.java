// com/google/javascript/jscomp/CodePrinterTest.java
public void testPrintInOperatorInHookExpression() {
    assertPrint("var a={};for(var i=a?(\"length\" in a):0;;);",
        "var a={};for(var i=a?(\"length\"in a):0;;);");
    assertPrint("var a={};for(var i=0?(\"x\" in a):(\"y\" in a);;);",
        "var a={};for(var i=0?(\"x\"in a):(\"y\"in a);;);");
  }