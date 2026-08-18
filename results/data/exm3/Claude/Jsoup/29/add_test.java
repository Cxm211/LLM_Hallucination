// org/jsoup/nodes/DocumentTest.java
@Test public void testTitleMultipleWhitespaceTypes() {
    Document doc = Jsoup.parse("<title>Hello\t\r\n  world</title>");
    assertEquals("Hello world", doc.title());
}