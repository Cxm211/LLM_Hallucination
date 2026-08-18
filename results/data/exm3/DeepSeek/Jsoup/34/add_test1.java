// org/jsoup/parser/CharacterReaderTest.java
@Test public void nextIndexOfInsufficientLengthAfterSkip() {
        CharacterReader r = new CharacterReader("xay");
        assertEquals(-1, r.nextIndexOf("ayz"));
    }
