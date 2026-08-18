// org/apache/commons/jxpath/ri/model/AliasedNamespaceIterationTest.java
public void testGetRelativePositionByQNameDOM() throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    org.w3c.dom.Document doc = db.newDocument();
    org.w3c.dom.Element root = doc.createElementNS("", "root");
    doc.appendChild(root);
    org.w3c.dom.Element e1 = doc.createElementNS("http://example.com", "a:foo");
    root.appendChild(e1);
    org.w3c.dom.Element e2 = doc.createElementNS("http://example.com", "b:foo");
    root.appendChild(e2);
    Class<?> clazz = Class.forName("org.apache.commons.jxpath.ri.model.dom.DOMNodePointer");
    java.lang.reflect.Constructor<?> ctor = clazz.getConstructor(org.w3c.dom.Node.class);
    Object pointer = ctor.newInstance(e2);
    java.lang.reflect.Method method = clazz.getDeclaredMethod("getRelativePositionByQName");
    method.setAccessible(true);
    int pos = (Integer) method.invoke(pointer);
    org.junit.Assert.assertEquals(2, pos);
}
