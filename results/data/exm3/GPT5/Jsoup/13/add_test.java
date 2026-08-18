// org/jsoup/nodes/NodeTest.java
@Test public void ignoresLiteralAbsAttributeOnHasAttr() {
        Document doc = Jsoup.parse("<a abs:href='http://malicious/'></a>", "http://jsoup.org/");
        Element a = doc.select("a").first();
        assertFalse(a.hasAttr("abs:href"));
    }