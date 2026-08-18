// org/jsoup/nodes/ElementTest.java
@Test public void testHtmlWhitespace() {
        // trailing whitespace
        Document doc1 = Jsoup.parse("<div>Hello </div>");
        Element div1 = doc1.select("div").first();
        assertEquals("Hello ", div1.html());
        // whitespace only
        Document doc2 = Jsoup.parse("<div>   </div>");
        Element div2 = doc2.select("div").first();
        assertEquals("   ", div2.html());
        // both leading and trailing whitespace
        Document doc3 = Jsoup.parse("<div>  foo  </div>");
        Element div3 = doc3.select("div").first();
        assertEquals("  foo  ", div3.html());
    }
