private static void handleRawtext(Token.StartTag startTag, HtmlTreeBuilder tb) {
    if (startTag.isSelfClosing()) {
        tb.insert(startTag);
        tb.pop();
        startTag.acknowledgeSelfClosingFlag();
    } else {
        tb.insert(startTag);
        tb.tokeniser.transition(TokeniserState.Rawtext);
        tb.markInsertionMode();
        tb.transition(Text);
    }
}