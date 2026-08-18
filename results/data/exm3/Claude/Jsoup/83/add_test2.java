// org/jsoup/parser/TokeniserStateTest.java
@Test public void handlesLessInTagNameWithAttribute() {
    String html = "<p<a href=\"test\">link</a>";
    Document doc = Jsoup.parse(html);
    assertEquals("<p></p><a href=\"test\">link</a>", TextUtil.stripNewlines(doc.body().html()));
}