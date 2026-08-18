// org/jsoup/nodes/NodeTest.java
@Test public void absHandlesRelativeQueryOnBaseWithQueryAndFragment() {
        Document doc = Jsoup.parse("<a href='?foo=bar'>One</a>", "http://example.com/dir/file.html?q=1#section");
        Element a = doc.select("a").first();
        assertEquals("http://example.com/dir/file.html?foo=bar", a.absUrl("href"));
    }
