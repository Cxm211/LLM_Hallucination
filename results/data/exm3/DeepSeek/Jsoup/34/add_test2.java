// org/jsoup/parser/CharacterReaderTest.java
@Test public void nextIndexOfSurrogateInsufficientLength() {
        CharacterReader r = new CharacterReader("\uD800");
        assertEquals(-1, r.nextIndexOf("\uD800\uDC00"));
    }
