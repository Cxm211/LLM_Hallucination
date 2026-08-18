// org/jsoup/nodes/ElementTest.java
@Test public void orphanedElementWithAttributes() {
    Element div = new Element("div");
    div.attr("class", "test");
    div.attr("id", "myDiv");
    assertEquals("<div class=\"test\" id=\"myDiv\"></div>", div.toString());
}