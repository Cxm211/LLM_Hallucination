// org/apache/commons/jxpath/ri/model/dom/DOMModelTest.java::testNodeAxisOnAttributes
public void testNodeAxisOnAttributes() {
        assertXPathNodeType(context, "//location/attribute::node()", Attr.class);
    }