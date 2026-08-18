// org/jsoup/helper/W3CDomTest.java::namespacePreservation
@Test
    public void namespaceScopeRollback() {
        String xml = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head/><body>" +
            "<div xmlns=\"urn:tmp\"><p id=\"a\"/></div>" +
            "<span id=\"b\"/></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(xml, "", Parser.xmlParser());

        org.jsoup.helper.W3CDom jDom = new org.jsoup.helper.W3CDom();
        Document doc = jDom.fromJsoup(jsoupDoc);

        Node html = doc.getChildNodes().item(0);
        Node body = html.getChildNodes().item(1);

        Node div = body.getFirstChild();
        assertEquals("urn:tmp", div.getNamespaceURI());
        Node p = div.getFirstChild();
        assertEquals("urn:tmp", p.getNamespaceURI());

        Node span = div.getNextSibling();
        assertEquals("http://www.w3.org/1999/xhtml", span.getNamespaceURI());
        assertEquals("span", span.getLocalName());
    }