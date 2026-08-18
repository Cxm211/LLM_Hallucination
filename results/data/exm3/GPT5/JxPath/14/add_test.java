// org/apache/commons/jxpath/ri/compiler/CoreFunctionTest.java::testCoreFunctions
assertXPathValue(context, "round('not a number')", new Double(Double.NaN));