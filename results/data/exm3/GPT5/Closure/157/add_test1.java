// com/google/javascript/jscomp/CodePrinterTest.java::testSetter
assertPrint("var x = {set '2'(y) {return 1}}", "var x={set 2(y){return 1}}");