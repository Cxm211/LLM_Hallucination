// org/apache/commons/csv/CSVLexerTest.java::testEscapedCharacter
@Test
public void testEscapedCharacterWithSlash() throws Exception {
    final CSVFormat fmt = CSVFormat.newBuilder(',').withEscape('/').build();
    final Lexer lexer = getLexer("character/aEscaped", fmt);
    assertThat(lexer.nextToken(new Token()), hasContent("character/aEscaped"));
}