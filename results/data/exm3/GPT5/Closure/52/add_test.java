// com/google/javascript/jscomp/CodePrinterTest.java::testNumericKeys
assertPrint("var x = {'00': 1};", "var x={\"00\":1}");