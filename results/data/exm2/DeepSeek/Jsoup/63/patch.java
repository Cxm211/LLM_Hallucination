Element insertEmpty(Token.StartTag startTag) {
    Tag tag = Tag.valueOf(startTag.name(), settings);
    Element el = new Element(tag, baseUri, startTag.attributes);
    insertNode(el);
    if (startTag.isSelfClosing()) {
        tokeniser.acknowledgeSelfClosingFlag();
        if (!tag.isKnownTag()) {
            tag.setSelfClosing();
        }
    }
    return el;
}