// org/jsoup/helper/W3CDomTest.java
@Test
public void nestedNamespaceSwitch() throws IOException {
    String html = "<root xmlns='http://default.ns'><a:outer xmlns:a='http://a.ns'><inner>Text</inner><a:inner2/></a:outer></root>";
    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "", Parser.xmlParser());

    org.jsoup.helper.W3CDom jDom = new org.jsoup.helper.W3CDom();
    Document doc = jDom.fromJsoup(jsoupDoc);

    Node root = doc.getChildNodes().item(0);
    assertEquals("http://default.ns", root.getNamespaceURI());

    Node outer = root.getChildNodes().item(0);
    assertEquals("http://a.ns", outer.getNamespaceURI());
    assertEquals("a:outer", outer.getNodeName());

    Node inner = outer.getChildNodes().item(0);
    assertEquals("http://default.ns", inner.getNamespaceURI());
    assertEquals("inner", inner.getLocalName());

    Node inner2 = outer.getChildNodes().item(1);
    assertEquals("http://a.ns", inner2.getNamespaceURI());
    assertEquals("a:inner2", inner2.getNodeName());
}