// com/google/javascript/jscomp/CodePrinterTest.java
public void testPrintInOperatorInNameInitializer() {
    assertPrint("var a={};for(var x=(\"length\" in a),y=1;;);",
        "var a={};for(var x=(\"length\"in a),y=1;;);");
    assertPrint("for(var i=1,j=(x?(\"a\" in z):0);;);",
        "for(var i=1,j=(x?(\"a\"in z):0);;);");
  }