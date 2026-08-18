// org/jsoup/nodes/EntitiesTest.java
@Test public void supplementaryPlaneCharacter() {
    String text = "&#128512;";
    assertEquals("\uD83D\uDE00", Entities.unescape(text));
}