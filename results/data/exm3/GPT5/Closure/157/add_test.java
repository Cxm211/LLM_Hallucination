// com/google/javascript/jscomp/CodePrinterTest.java::testGetter
assertPrint("var x = {get '2'() {return 1}}", "var x={get 2(){return 1}}");