// org/jsoup/nodes/NodeTest.java
@Test public void absHandlesRelativeQueryOnBaseWithoutQuery() {
        Document doc = Jsoup.parse("<a href='?foo'>One</a>", "http://example.com/path/file");
        Element a = doc.select("a").first();
        assertEquals("http://example.com/path/file?foo", a.absUrl("href"));
    }
