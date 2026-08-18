// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeZeroCharValue() {
    String escaped = "&#0;";
    String expected = "\u0000";
    assertEquals(expected, Entities.unescape(escaped));
}