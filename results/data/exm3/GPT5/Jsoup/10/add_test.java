// org/jsoup/nodes/NodeTest.java
@Test public void absHandlesRelativeQueryNoBaseQuery() {
        Document doc = Jsoup.parse("<a href='?foo'>One</a>", "http://jsoup.org/path/file");

        Element a1 = doc.selectFirst("a");
        assertEquals("http://jsoup.org/path/file?foo", a1.absUrl("href"));
    }