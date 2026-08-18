// org/jsoup/parser/CharacterReaderTest.java
@Test public void consumeToEndEmptyInput() {
        String in = "";
        CharacterReader r = new CharacterReader(in);
        String toEnd = r.consumeToEnd();
        assertEquals("", toEnd);
        assertTrue(r.isEmpty());
    }
