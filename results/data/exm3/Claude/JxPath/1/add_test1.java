// org/apache/commons/jxpath/ri/model/jdom/JDOMModelTest.java
public void testGetNodeNameMismatch() {
    assertXPathPointerEquals("/vendor/location[name()!='location']", context, "<<empty>>");
}