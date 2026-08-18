// ===== FIXED org.jsoup.parser.TokeniserState :: read(Tokeniser, CharacterReader) [lines 9-29] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-31-fixed/src/main/java/org/jsoup/parser/TokeniserState.java =====
        void read(Tokeniser t, CharacterReader r) {
            switch (r.current()) {
                case '&':
                    t.advanceTransition(CharacterReferenceInData);
                    break;
                case '<':
                    t.advanceTransition(TagOpen);
                    break;
                case nullChar:
                    t.error(this); // NOT replacement character (oddly?)
                    t.emit(r.consume());
                    break;
                case eof:
                    t.emit(new Token.EOF());
                    break;
                default:
                    String data = r.consumeToAny('&', '<', nullChar);
                    t.emit(data);
                    break;
            }
        }

// ===== FIXED org.jsoup.parser.XmlTreeBuilder :: insert(Token) [lines 49-62] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-31-fixed/src/main/java/org/jsoup/parser/XmlTreeBuilder.java =====
    Element insert(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name());
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.
        Element el = new Element(tag, baseUri, startTag.attributes);
        insertNode(el);
        if (startTag.isSelfClosing()) {
            tokeniser.acknowledgeSelfClosingFlag();
            if (!tag.isKnownTag()) // unknown tag, remember this is self closing for output. see above.
                tag.setSelfClosing();
        } else {
            stack.add(el);
        }
        return el;
    }
