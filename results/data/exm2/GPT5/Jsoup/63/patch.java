    Element insertEmpty(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name(), settings);
        Element el = new Element(tag, baseUri, startTag.attributes);
        insertNode(el);
        if (startTag.isSelfClosing()) {
            if (tag.isKnownTag()) {
                if (tag.isSelfClosing()) {
                    tokeniser.acknowledgeSelfClosingFlag();
                } else {
                    error("Tag cannot be self closing; not a void tag");
                    tokeniser.acknowledgeSelfClosingFlag();
                }
            } else {
                tag.setSelfClosing();
                tokeniser.acknowledgeSelfClosingFlag();
            }
        }
        return el;
    }