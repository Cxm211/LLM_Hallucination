// org/jsoup/integration/ParseTest.java
@Test(expected = IllegalArgumentException.class)
    public void testCharacterReaderInvalidBufferSize() {
        new CharacterReader(new StringReader("test"), 0);
    }
