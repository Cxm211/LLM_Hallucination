// org/jsoup/nodes/EntitiesTest.java::escapesLtAndAmpInXmlAttributes
@Test public void escapesLtAndAmpInXmlAttributes() {
        String docHtml = "<a title='A < B & C'>X</a>";
        Document doc = Jsoup.parse(docHtml);
        Element element = doc.select("a").first();

        doc.outputSettings().escapeMode(xhtml);
        assertEquals("<a title=\"A &lt; B &amp; C\">X</a>", element.outerHtml());
    }