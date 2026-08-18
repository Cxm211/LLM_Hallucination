// org/apache/commons/csv/CSVLexerTest.java
@Test
    public void testEscapedDigit() throws Exception {
        final Lexer lexer = getLexer("character\\1Escaped", formatWithEscaping);
        assertThat(lexer.nextToken(new Token()), hasContent("character\\1Escaped"));
    }
