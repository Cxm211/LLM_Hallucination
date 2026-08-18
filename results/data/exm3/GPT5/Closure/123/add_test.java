// com/google/javascript/jscomp/CodePrinterTest.java::testPrintInOperatorInForLoop
assertPrint("var a={}; for (var i = +(\"length\" in a); i;) {}","var a={};for(var i=+(\"length\"in a);i;);");