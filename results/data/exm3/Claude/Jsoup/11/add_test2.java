// org/jsoup/select/SelectorTest.java
@Test public void notWithAttribute() {
    Document doc = Jsoup.parse("<p data-val=1>One</p><p>Two</p><p data-val=2>Three</p>");
    Elements el1 = doc.select("p:not([data-val])");
    assertEquals(1, el1.size());
    assertEquals("Two", el1.first().text());
}