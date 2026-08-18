// org/jsoup/nodes/NodeTest.java
@Test public void absHandlesRelativeQueryWithoutFile() {
        Document doc = Jsoup.parse("<a href='?foo'>One</a>", "http://jsoup.org/path/");
        Element a1 = doc.select("a").first();
        assertEquals("http://jsoup.org/path/?foo", a1.absUrl("href"));
    }