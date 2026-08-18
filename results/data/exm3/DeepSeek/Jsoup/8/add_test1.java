// org/jsoup/nodes/ElementTest.java
@Test public void detachedTextNodeToString() {
    TextNode text = new TextNode("Hello");
    assertEquals("Hello", text.toString());
}
