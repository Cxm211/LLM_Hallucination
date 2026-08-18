// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testUnionThreeElements() {
    assertXPathValue(context, "/vendor[1]/contact[1] | /vendor[1]/contact[2] | /vendor[1]/contact[3]", "John");
}