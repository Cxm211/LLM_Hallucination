// org/jsoup/select/SelectorTest.java
@Test public void notWithTagAndClass() {
    Document doc = Jsoup.parse("<div><p class=x>One</p><span class=x>Two</span><p>Three</p></div>");
    Elements el1 = doc.select("p:not(.x)");
    assertEquals(1, el1.size());
    assertEquals("Three", el1.first().text());
}