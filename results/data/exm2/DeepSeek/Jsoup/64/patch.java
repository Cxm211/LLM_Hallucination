    private static void handleRawtext(Token.StartTag startTag, HtmlTreeBuilder tb) {
        tb.insert(startTag);
        if (!startTag.isSelfClosing()) {
            tb.tokeniser.transition(TokeniserState.Rawtext);
            tb.markInsertionMode();
            tb.transition(Text);
        }
    }