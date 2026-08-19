// ===== FIXED org.jsoup.nodes.Attributes :: add(String, String) [lines 119-125] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-92-fixed/src/main/java/org/jsoup/nodes/Attributes.java =====
    public Attributes add(String key, String value) {
        checkCapacity(size + 1);
        keys[size] = key;
        vals[size] = value;
        size++;
        return this;
    }

// ===== FIXED org.jsoup.parser.HtmlTreeBuilder :: insert(Token) [lines 197-219] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-92-fixed/src/main/java/org/jsoup/parser/HtmlTreeBuilder.java =====
    Element insert(final Token.StartTag startTag) {
        // cleanup duplicate attributes:
        if (!startTag.attributes.isEmpty()) {
            int dupes = startTag.attributes.deduplicate(settings);
            if (dupes > 0) {
                error("Duplicate attribute");
            }
        }

        // handle empty unknown tags
        // when the spec expects an empty tag, will directly hit insertEmpty, so won't generate this fake end tag.
        if (startTag.isSelfClosing()) {
            Element el = insertEmpty(startTag);
            stack.add(el);
            tokeniser.transition(TokeniserState.Data); // handles <script />, otherwise needs breakout steps from script data
            tokeniser.emit(emptyEnd.reset().name(el.tagName()));  // ensure we get out of whatever state we are in. emitted for yielded processing
            return el;
        }

        Element el = new Element(Tag.valueOf(startTag.name(), settings), baseUri, settings.normalizeAttributes(startTag.attributes));
        insert(el);
        return el;
    }

// ===== FIXED org.jsoup.parser.XmlTreeBuilder :: insert(Token) [lines 75-89] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-92-fixed/src/main/java/org/jsoup/parser/XmlTreeBuilder.java =====
    Element insert(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name(), settings);
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.
        startTag.attributes.deduplicate(settings);

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
