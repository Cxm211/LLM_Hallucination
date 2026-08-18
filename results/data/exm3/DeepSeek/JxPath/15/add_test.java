// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testUnionOrdering() {
    // Test union across different parents, document order should bring vendor[1] first
    assertXPathValue(context, "/vendor[2]/contact[1] | /vendor[1]/contact[1]", "John");
    // Test union of three nodes in reverse order
    assertXPathValue(context, "/vendor[1]/contact[3] | /vendor[1]/contact[2] | /vendor[1]/contact[1]", "John");
}
