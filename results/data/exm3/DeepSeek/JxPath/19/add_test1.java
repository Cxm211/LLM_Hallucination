// org/apache/commons/jxpath/ri/model/AliasedNamespaceIterationTest.java
public void testGetRelativePositionByQNameJDOM() throws Exception {
    org.jdom.Document doc = new org.jdom.Document();
    org.jdom.Element root = new org.jdom.Element("root");
    doc.setRootElement(root);
    org.jdom.Namespace ns = org.jdom.Namespace.getNamespace("http://example.com");
    org.jdom.Element e1 = new org.jdom.Element("foo", ns);
    e1.setNamespace(org.jdom.Namespace.getNamespace("a", "http://example.com"));
    root.addContent(e1);
    org.jdom.Element e2 = new org.jdom.Element("foo", ns);
    e2.setNamespace(org.jdom.Namespace.getNamespace("b", "http://example.com"));
    root.addContent(e2);
    Class<?> clazz = Class.forName("org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer");
    java.lang.reflect.Constructor<?> ctor = clazz.getConstructor(org.jdom.Element.class);
    Object pointer = ctor.newInstance(e2);
    java.lang.reflect.Method method = clazz.getDeclaredMethod("getRelativePositionByQName");
    method.setAccessible(true);
    int pos = (Integer) method.invoke(pointer);
    org.junit.Assert.assertEquals(2, pos);
}
