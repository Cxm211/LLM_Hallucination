// org/jsoup/parser/AttributeParseTest.java
@Test public void dropsSlashWithoutSpaceFromAttributeName() {
        String html = "<img/onclick='x'>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.select("img[onclick]").size() != 0);
        assertEquals("<img onclick=\"x\">", doc.body().html());
    }