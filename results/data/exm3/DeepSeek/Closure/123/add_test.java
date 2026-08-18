// com/google/javascript/jscomp/CodePrinterTest.java
public void testPrintInOperatorInForLoopMultipleVar() {
    assertPrint("var a={}; for (var i = 0, j = (\"length\" in a); i;) {}",
        "var a={};for(var i=0,j=(\"length\"in a);i;);");
}
