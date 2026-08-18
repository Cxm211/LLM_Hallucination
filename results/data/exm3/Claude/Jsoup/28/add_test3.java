// org/jsoup/nodes/EntitiesTest.java
@Test public void charValueJustOverBoundary() {
    String text = "&#65536;";
    String result = Entities.unescape(text);
    assertEquals("&#65536;", result);
}