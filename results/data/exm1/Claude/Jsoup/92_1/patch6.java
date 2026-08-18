Element insert(Token.StartTag startTag) {
    Tag tag = Tag.valueOf(startTag.name(), settings);

    Element el = new Element(tag, baseUri, settings.normalizeAttributes(startTag.attributes));
    insertNode(el);
    if (startTag.isSelfClosing()) {
        if (!tag.isKnownTag())
            tag.setSelfClosing();
    } else {
        stack.add(el);
    }
    return el;
}