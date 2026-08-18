// com/google/javascript/jscomp/CodePrinterTest.java::testIssue942_additional
public void testIssue942_additional() { assertPrint("var x = {a: 0, 0: 1, b: 2};", "var x={a:0,0:1,b:2}"); }