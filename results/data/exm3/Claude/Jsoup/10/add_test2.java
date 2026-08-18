// org/jsoup/nodes/NodeTest.java
@Test public void absHandlesRelativeQueryWithPort() {
        Document doc = Jsoup.parse("<a href='?foo'>One</a>", "http://jsoup.org:8080/path/file?bar");
        Element a1 = doc.select("a").first();
        assertEquals("http://jsoup.org:8080/path/file?foo", a1.absUrl("href"));
    }