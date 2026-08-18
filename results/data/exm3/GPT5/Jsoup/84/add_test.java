// org/jsoup/helper/W3CDomTest.java::unprefixedElementsHaveNullNamespace
@Test public void unprefixedElementsHaveNullNamespace() {
        String html = "<div><p>t</p></div>";
        org.jsoup.nodes.Document doc = Jsoup.parse(html);

        Document w3Doc = new W3CDom().fromJsoup(doc);
        Node p = w3Doc.getElementsByTagName("p").item(0);
        assertNotNull(p);
        assertNull(p.getNamespaceURI());
        assertEquals("p", p.getLocalName());
        assertEquals("p", p.getNodeName());
    }