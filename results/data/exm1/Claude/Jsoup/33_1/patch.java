Element insert(Token.StartTag startTag) {
        if (startTag.isSelfClosing()) {
            Element el = insertEmpty(startTag);
            tokeniser.emit(new Token.EndTag(el.tagName()));
            return el;
        }
        
        Element el = new Element(Tag.valueOf(startTag.name()), baseUri, startTag.attributes);
        insert(el);
        return el;
    }