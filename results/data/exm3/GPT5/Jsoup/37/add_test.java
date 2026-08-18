// org/jsoup/nodes/ElementTest.java
@Test public void testPreserveTrailingWhitespace() {
        Document doc = Jsoup.parse("<div><span>Hi</span>  </div>");
        doc.outputSettings().prettyPrint(false);
        Element div = doc.selectFirst("div");
        assertEquals("<span>Hi</span>  ", div.html());
    }