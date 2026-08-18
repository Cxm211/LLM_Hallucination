Element insert(Token.StartTag startTag) {
    // handle empty unknown tags
    // when the spec expects an empty tag, will directly hit insertEmpty, so won't generate this fake end tag.
    Tag tag = Tag.valueOf(startTag.name());
    if (startTag.isSelfClosing() && (!tag.isKnownTag() || tag.isEmpty())) {
        Element el = insertEmpty(startTag);
        stack.add(el);
        tokeniser.emit(new Token.EndTag(el.tagName()));  // ensure we get out of whatever state we are in. emitted for yielded processing
        return el;
    }
    
    Element el = new Element(tag, baseUri, startTag.attributes);
    insert(el);
    return el;
}