// org/jsoup/select/SelectorTest.java
@Test public void handlesCommaWithSingleSelector() {
    Document doc = Jsoup.parse("<p>One</p><div>Two</div>");
    Elements els = doc.select("p,");
    assertEquals(1, els.size());
    assertEquals("p", els.get(0).tagName());
}