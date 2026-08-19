// ===== FIXED org.jsoup.parser.HtmlTreeBuilderState :: handleRawtext(Token, HtmlTreeBuilder) [lines 1488-1493] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-64-fixed/src/main/java/org/jsoup/parser/HtmlTreeBuilderState.java =====
    private static void handleRawtext(Token.StartTag startTag, HtmlTreeBuilder tb) {
        tb.tokeniser.transition(TokeniserState.Rawtext);
        tb.markInsertionMode();
        tb.transition(Text);
        tb.insert(startTag);
    }
