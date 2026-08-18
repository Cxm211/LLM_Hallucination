// ===== FIXED org.jsoup.parser.XmlTreeBuilder :: insert(Token) [lines 69-81] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-80-fixed/src/main/java/org/jsoup/parser/XmlTreeBuilder.java =====
    Element insert(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name(), settings);
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.
        Element el = new Element(tag, baseUri, settings.normalizeAttributes(startTag.attributes));
        insertNode(el);
        if (startTag.isSelfClosing()) {
            if (!tag.isKnownTag()) // unknown tag, remember this is self closing for output. see above.
                tag.setSelfClosing();
        } else {
            stack.add(el);
        }
        return el;
    }
