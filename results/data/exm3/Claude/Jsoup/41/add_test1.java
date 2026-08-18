// org/jsoup/nodes/ElementTest.java
@Test
public void testEqualsSameReference() {
    String html = "<p class=\"test\">Content</p>";
    Document doc = Jsoup.parse(html);
    Element el = doc.select("p").first();
    
    assertTrue(el.equals(el));
}