// org/jsoup/nodes/ElementTest.java
@Test
public void testEqualsNullAndDifferentClass() {
    String html = "<div><p>Test</p></div>";
    Document doc = Jsoup.parse(html);
    Element el = doc.select("p").first();
    
    assertFalse(el.equals(null));
    assertFalse(el.equals("not an element"));
}