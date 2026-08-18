    String consumeTagName() {
        // '\t', '\n', '\r', '\f', ' ', '/', '>', nullChar
        // NOTE: out of spec, added '<' to fix common author bugs
        bufferUp();
        final int start = bufPos;
        final int remaining = bufLength;
        final char[] val = charBuf;

        while (bufPos < remaining) {
            final char c = val[bufPos];
            if (c == '\t'|| c ==  '\n'|| c ==  '\r'|| c ==  '\f'|| c ==  ' '|| c ==  '/'|| c ==  '>'|| c ==  TokeniserState.nullChar)
                break;
            bufPos++;
        }

        return bufPos > start ? cacheString(charBuf, stringCache, start, bufPos -start) : "";
    }

        void read(Tokeniser t, CharacterReader r) {
            // previous TagOpen state did NOT consume, will have a letter char in current
            //String tagName = r.consumeToAnySorted(tagCharsSorted).toLowerCase();
            String tagName = r.consumeTagName();
            t.tagPending.appendTagName(tagName);

            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    t.transition(BeforeAttributeName);
                    break;
                case '/':
                    t.transition(SelfClosingStartTag);
                    break;
                    // intended fall through to next >
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case nullChar: // replacement
                    t.tagPending.appendTagName(replacementStr);
                    break;
                case eof: // should emit pending tag?
                    t.eofError(this);
                    t.transition(Data);
                    break;
                default: // buffer underrun
                    t.tagPending.appendTagName(c);
            }
        }

        void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case '\f':
                case ' ':
                    break; // ignore whitespace
                case '/':
                    t.transition(SelfClosingStartTag);
                    break;
                    // intended fall through as if >
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case nullChar:
                    t.error(this);
                    t.tagPending.newAttribute();
                    r.unconsume();
                    t.transition(AttributeName);
                    break;
                case eof:
                    t.eofError(this);
                    t.transition(Data);
                    break;
                case '"':
                case '\'':
                case '<':
                case '=':
                    t.error(this);
                    t.tagPending.newAttribute();
                    t.tagPending.appendAttributeName(c);
                    t.transition(AttributeName);
                    break;
                default: // A-Z, anything else
                    t.tagPending.newAttribute();
                    r.unconsume();
                    t.transition(AttributeName);
            }
        }

// trigger testcase
@Test public void parsesQuiteRoughAttributes() {
        String html = "<p =a>One<a <p>Something</p>Else";
        // this (used to; now gets cleaner) gets a <p> with attr '=a' and an <a tag with an attribue named '<p'; and then auto-recreated
        Document doc = Jsoup.parse(html);

        // NOTE: per spec this should be the test case. but impacts too many ppl
        // assertEquals("<p =a>One<a <p>Something</a></p>\n<a <p>Else</a>", doc.body().html());

        assertEquals("<p =a>One<a></a></p><p><a>Something</a></p><a>Else</a>", TextUtil.stripNewlines(doc.body().html()));

        doc = Jsoup.parse("<p .....>");
        assertEquals("<p .....></p>", doc.body().html());
    }

@Test public void handlesLessInTagThanAsNewTag() {
        // out of spec, but clear author intent
        String html = "<p\n<p<div id=one <span>Two";
        Document doc = Jsoup.parse(html);
        assertEquals("<p></p><p></p><div id=\"one\"><span>Two</span></div>", TextUtil.stripNewlines(doc.body().html()));
    }
