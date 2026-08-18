// org/apache/commons/jxpath/ri/model/dom/DOMModelTest.java
public void testGetNodeNameMismatch() {
    assertXPathPointerEquals("/vendor/location[name()!='location']", context, "<<empty>>");
}