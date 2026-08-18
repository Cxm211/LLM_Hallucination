// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java::testNodeSetOperations
assertXPathValue(context, "0 < $array", Boolean.TRUE, Boolean.class);
assertXPathValue(context, "0 > $array", Boolean.FALSE, Boolean.class);