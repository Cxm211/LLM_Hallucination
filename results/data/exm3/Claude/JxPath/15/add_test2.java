// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testUnionSingleElement() {
    assertXPathValue(context, "/vendor[1]/contact[1] | /vendor[1]/contact[1]", "John");
}