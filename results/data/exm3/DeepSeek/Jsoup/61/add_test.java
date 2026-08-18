// org/jsoup/select/ElementsTest.java
@Test public void hasClassMultipleClasses() {
    Element el = Jsoup.parse("<div class=\"foo bar\">").select("div").first();
    assertTrue(el.hasClass("foo"));
    assertTrue(el.hasClass("bar"));
    assertFalse(el.hasClass("foo bar"));
    assertFalse(el.hasClass("FOO BAR"));
}
