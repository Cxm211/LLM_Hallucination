// org/jsoup/nodes/ElementTest.java
@Test
public void testEqualsDifferentAttributes() {
    String html = "<div><p id=\"1\" class=\"a\">Text</p><p id=\"2\" class=\"a\">Text</p></div>";
    Document doc = Jsoup.parse(html);
    Elements els = doc.select("p");
    Element e0 = els.get(0);
    Element e1 = els.get(1);
    
    assertFalse(e0.equals(e1));
}