// org/jsoup/parser/CharacterReaderTest.java
@Test public void nextIndexOfPartialMatchAtEnd() {
    CharacterReader r = new CharacterReader("ab");
    assertEquals(-1, r.nextIndexOf("abc"));
}