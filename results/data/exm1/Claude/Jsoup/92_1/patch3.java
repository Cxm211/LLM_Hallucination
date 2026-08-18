Element insert(final Token.StartTag startTag) {
    if (startTag.isSelfClosing()) {
        Element el = insertEmpty(startTag);
        stack.add(el);
        tokeniser.transition(TokeniserState.Data);
        tokeniser.emit(emptyEnd.reset().name(el.tagName()));
        return el;
    }

    Element el = new Element(Tag.valueOf(startTag.name(), settings), baseUri, settings.normalizeAttributes(startTag.attributes));
    insert(el);
    return el;
}