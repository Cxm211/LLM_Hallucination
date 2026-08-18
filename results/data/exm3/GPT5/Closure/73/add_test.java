// com/google/javascript/jscomp/CodePrinterTest.java::testUnicode
assertPrint("var x ='\\x7f\\x68';", "var x=\"\\u007fh\"");