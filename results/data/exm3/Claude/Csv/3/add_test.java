// org/apache/commons/csv/CSVLexerTest.java
@Test
public void testEscapedDigit() throws Exception {
    final Lexer lexer = getLexer("value\\1test", formatWithEscaping);
    assertThat(lexer.nextToken(new Token()), hasContent("value\\1test"));
}