// com/google/javascript/jscomp/CodePrinterTest.java::testDoLoopIECompatiblity
assertPrint("A:{do{foo()}while(y)}", "A:{do foo();while(y)}");