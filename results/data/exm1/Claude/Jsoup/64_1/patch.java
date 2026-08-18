private static void handleRawtext(Token.StartTag startTag, HtmlTreeBuilder tb) {
    tb.insert(startTag);
    tb.markInsertionMode();
    tb.tokeniser.transition(TokeniserState.Rawtext);
    tb.transition(Text);
}