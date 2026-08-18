    private static void handleRawtext(Token.StartTag startTag, HtmlTreeBuilder tb) {
        startTag.selfClosing = false;
        tb.insert(startTag);
        tb.tokeniser.transition(TokeniserState.Rawtext);
        tb.markInsertionMode();
        tb.transition(Text);
    }