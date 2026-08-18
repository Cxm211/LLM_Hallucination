// org/jsoup/parser/CharacterReaderTest.java
@Test public void nextIndexOfSequenceExactLength() {
    CharacterReader r = new CharacterReader("abc");
    assertEquals(0, r.nextIndexOf("abc"));
}