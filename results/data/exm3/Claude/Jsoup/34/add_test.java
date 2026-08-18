// org/jsoup/parser/CharacterReaderTest.java
@Test public void nextIndexOfSequenceAtEnd() {
    CharacterReader r = new CharacterReader("abc");
    assertEquals(-1, r.nextIndexOf("abcd"));
}