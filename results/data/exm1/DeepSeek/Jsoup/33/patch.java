Element insert(Token.StartTag startTag) {
    Tag tag = Tag.valueOf(startTag.name());
    if (startTag.isSelfClosing()) {
        if (tag.isKnownEmpty()) {
            return insertEmpty(startTag);
        } else if (!tag.isKnown()) {
            Element el = insertEmpty(startTag);
            stack.add(el);
            tokeniser.emit(new Token.EndTag(el.tagName()));
            return el;
        }
    }
    Element el = new Element(tag, baseUri, startTag.attributes);
    insert(el);
    return el;
}