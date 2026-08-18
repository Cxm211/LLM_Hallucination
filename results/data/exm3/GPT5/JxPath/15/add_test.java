// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java::testUnion
assertXPathValue(context, "/vendor[1]/contact[4] | /vendor[1]/contact[1] | /vendor[1]/contact[3]", "John");