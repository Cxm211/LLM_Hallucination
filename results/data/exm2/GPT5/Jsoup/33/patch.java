Element insert(Token.StartTag startTag) {
    if (startTag.isSelfClosing()) {
        Tag tag = Tag.valueOf(startTag.name());
        if (tag.isKnownTag() && !tag.isEmpty()) {
            Element el = new Element(tag, baseUri, startTag.attributes);
            insert(el);
            tokeniser.emit(new Token.EndTag(el.tagName()));
            return el;
        } else {
            return insertEmpty(startTag);
        }
    }

    Element el = new Element(Tag.valueOf(startTag.name()), baseUri, startTag.attributes);
    insert(el);
    return el;
}