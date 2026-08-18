// org/jsoup/nodes/NodeTest.java
@Test public void absHandlesRelativeQueryAtRoot() {
        Document doc = Jsoup.parse("<a href='?foo'>One</a>", "http://jsoup.org/");
        Element a1 = doc.select("a").first();
        assertEquals("http://jsoup.org/?foo", a1.absUrl("href"));
    }