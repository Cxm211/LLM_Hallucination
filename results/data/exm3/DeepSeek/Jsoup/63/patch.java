    Element insertEmpty(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name(), settings);
        Element el = new Element(tag, baseUri, startTag.attributes);
        insertNode(el);
        if (startTag.isSelfClosing()) {
            if (!tag.isKnownTag()) {
                tag.setSelfClosing();
            }
            tokeniser.acknowledgeSelfClosingFlag();
        }
        return el;
    }