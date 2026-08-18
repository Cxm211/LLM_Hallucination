// org/apache/commons/jxpath/ri/model/jdom/JDOMModelTest.java::testNodeAxisOnAttributes
public void testNodeAxisOnAttributes() {
        assertXPathNodeType(context, "//location/attribute::node()", Attribute.class);
    }