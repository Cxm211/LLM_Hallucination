// org/jsoup/parser/TokeniserStateTest.java
@Test public void handlesLessInTagNameFollowedBySlash() {
    String html = "<div</>content";
    Document doc = Jsoup.parse(html);
    assertEquals("<div></div>content", TextUtil.stripNewlines(doc.body().html()));
}