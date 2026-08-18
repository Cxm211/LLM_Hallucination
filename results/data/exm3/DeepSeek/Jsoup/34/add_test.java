// org/jsoup/parser/CharacterReaderTest.java
@Test public void nextIndexOfInsufficientLengthImmediate() {
        CharacterReader r = new CharacterReader("ab");
        assertEquals(-1, r.nextIndexOf("abc"));
    }
