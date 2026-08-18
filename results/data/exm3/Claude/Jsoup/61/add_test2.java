// org/jsoup/select/ElementsTest.java
@Test public void hasClassWithMultipleWhitespaces() {
    Document doc = Jsoup.parse("<p class=\"  foo   bar  \">Text</p>");
    Element p = doc.select("p").first();
    assertTrue(p.hasClass("FOO"));
    assertTrue(p.hasClass("Bar"));
    assertFalse(p.hasClass("baz"));
}