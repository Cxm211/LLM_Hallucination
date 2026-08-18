// org/apache/commons/jxpath/ri/model/jdom/JDOMModelTest.java
public void testNodeTypeTests() {
    // Test node() test for document and attribute nodes
    Document doc = (Document) context.getContextBean();
    NodeTest nodeTest = new org.apache.commons.jxpath.ri.compiler.NodeTypeTest(org.apache.commons.jxpath.ri.compiler.Compiler.NODE_TYPE_NODE);
    boolean result = org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer.testNode(null, doc, nodeTest);
    assertTrue("Document node should pass node() test", result);
    Attribute attr = doc.getRootElement().getChild("location").getAttribute("name");
    NodeTest attrTest = new org.apache.commons.jxpath.ri.compiler.NodeTypeTest(org.apache.commons.jxpath.ri.compiler.Compiler.NODE_TYPE_ATTRIBUTE);
    result = org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer.testNode(null, attr, attrTest);
    assertTrue("Attribute node should pass attribute test", result);
}
