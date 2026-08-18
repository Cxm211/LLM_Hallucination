// org/jsoup/parser/AttributeParseTest.java
@Test public void dropsSlashBeforeDigitAttribute() {
    String html = "<img /5data='test'>";
    Document doc = Jsoup.parse(html);
    assertTrue(doc.select("img[5data]").size() != 0);
    assertEquals("<img 5data=\"test\">", doc.body().html());
}
