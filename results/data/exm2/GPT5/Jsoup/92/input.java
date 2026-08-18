    private void add(String key, String value) {
        checkCapacity(size + 1);
        keys[size] = key;
        vals[size] = value;
        size++;
    }

    public int size() {
        return size;
    }

    public void normalize() {
        for (int i = 0; i < size; i++) {
            keys[i] = lowerCase(keys[i]);
        }
    }

    Element insert(final Token.StartTag startTag) {
        // cleanup duplicate attributes:

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

    public boolean preserveTagCase() {
        return preserveTagCase;
    }

        final void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();

            if (pendingAttributeName != null) {
                // the tokeniser has skipped whitespace control chars, but trimming could collapse to empty for other control codes, so verify here
                pendingAttributeName = pendingAttributeName.trim();
                if (pendingAttributeName.length() > 0) {
                    String value;
                    if (hasPendingAttributeValue)
                        value = pendingAttributeValue.length() > 0 ? pendingAttributeValue.toString() : pendingAttributeValueS;
                    else if (hasEmptyAttributeValue)
                        value = "";
                    else
                        value = null;
                    // note that we add, not put. So that the first is kept, and rest are deduped, once in a context where case sensitivity is known (the appropriate tree builder).
                    attributes.put(pendingAttributeName, value);
                }
            }
            pendingAttributeName = null;
            hasEmptyAttributeValue = false;
            hasPendingAttributeValue = false;
            reset(pendingAttributeValue);
            pendingAttributeValueS = null;
        }

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

// trigger testcase
@Test public void dropsDuplicateAttributes() {
        String html = "<p One=One ONE=Two Two=two one=Three One=Four two=Five>Text</p>";
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");

        Element p = doc.selectFirst("p");
        assertEquals("<p one=\"One\" two=\"two\">Text</p>", p.outerHtml()); // normalized names due to lower casing

        assertEquals(1, parser.getErrors().size());
        assertEquals("Duplicate attribute", parser.getErrors().get(0).getErrorMessage());
    }

@Test public void retainsAttributesOfDifferentCaseIfSensitive() {
        String html = "<p One=One One=Two one=Three two=Four two=Five Two=Six>Text</p>";
        Parser parser = Parser.htmlParser().settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        assertEquals("<p One=\"One\" one=\"Three\" two=\"Four\" Two=\"Six\">Text</p>", doc.selectFirst("p").outerHtml());
    }

@Test public void dropsDuplicateAttributes() {
        // case sensitive, so should drop Four and Five
        String html = "<p One=One ONE=Two one=Three One=Four ONE=Five two=Six two=Seven Two=Eight>Text</p>";
        Parser parser = Parser.xmlParser().setTrackErrors(10);
        Document doc = parser.parseInput(html, "");

        assertEquals("<p One=\"One\" ONE=\"Two\" one=\"Three\" two=\"Six\" Two=\"Eight\">Text</p>", doc.selectFirst("p").outerHtml());
    }
