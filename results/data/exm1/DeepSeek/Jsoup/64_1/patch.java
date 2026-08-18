    private static void handleRawtext(Token.StartTag startTag, HtmlTreeBuilder tb) {
        tb.insert(startTag);
        if (startTag.getName().equals("textarea") || startTag.getName().equals("title")) {
            tb.tokeniser.transition(TokeniserState.Rcdata);
        } else {
            tb.tokeniser.transition(TokeniserState.Rawtext);
        }
        tb.markInsertionMode();
        tb.transition(Text);
    }