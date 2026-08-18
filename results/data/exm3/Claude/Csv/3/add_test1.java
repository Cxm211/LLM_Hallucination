// org/apache/commons/csv/CSVLexerTest.java
@Test
public void testEscapedSpecialChar() throws Exception {
    final Lexer lexer = getLexer("data\\@symbol", formatWithEscaping);
    assertThat(lexer.nextToken(new Token()), hasContent("data\\@symbol"));
}