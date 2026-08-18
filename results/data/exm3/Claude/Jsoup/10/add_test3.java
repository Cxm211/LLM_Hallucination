// org/jsoup/nodes/NodeTest.java
@Test public void absHandlesAbsoluteUrl() {
        Document doc = Jsoup.parse("<a href='http://example.com/page'>One</a>", "http://jsoup.org/path/file");
        Element a1 = doc.select("a").first();
        assertEquals("http://example.com/page", a1.absUrl("href"));
    }