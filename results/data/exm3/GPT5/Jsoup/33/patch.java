Element insert(Token.StartTag startTag) {
        if (startTag.isSelfClosing()) {
            Tag tag = Tag.valueOf(startTag.name());
            // For known, non-empty (non-void) tags: create element, then immediately close via synthetic end tag
            if (tag.isKnownTag() && !tag.isEmpty()) {
                Element el = new Element(tag, baseUri, startTag.attributes);
                insert(el);
                tokeniser.emit(new Token.EndTag(el.tagName()));
                return el;
            }
            // For known empty (void) tags and unknown tags: treat as empty, do not emit a synthetic end tag
            return insertEmpty(startTag);
        }
        Element el = new Element(Tag.valueOf(startTag.name()), baseUri, startTag.attributes);
        insert(el);
        return el;
    }