// ===== FIXED org.jsoup.parser.CharacterReader :: consumeTagName() [lines 244-260] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-83-fixed/src/main/java/org/jsoup/parser/CharacterReader.java =====
    String consumeTagName() {
        // '\t', '\n', '\r', '\f', ' ', '/', '>', nullChar
        // NOTE: out of spec, added '<' to fix common author bugs
        bufferUp();
        final int start = bufPos;
        final int remaining = bufLength;
        final char[] val = charBuf;

        while (bufPos < remaining) {
            final char c = val[bufPos];
            if (c == '\t'|| c ==  '\n'|| c ==  '\r'|| c ==  '\f'|| c ==  ' '|| c ==  '/'|| c ==  '>'|| c == '<' || c ==  TokeniserState.nullChar)
                break;
            bufPos++;
        }

        return bufPos > start ? cacheString(charBuf, stringCache, start, bufPos -start) : "";
    }

// ===== FIXED org.jsoup.parser.TokeniserState :: read(Tokeniser, CharacterReader) [lines 11-31] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-83-fixed/src/main/java/org/jsoup/parser/TokeniserState.java =====
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
                    String data = r.consumeData();
                    t.emit(data);
                    break;
            }
        }

// ===== FIXED org.jsoup.parser.TokeniserState :: read(Tokeniser, CharacterReader) [lines 11-31] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-83-fixed/src/main/java/org/jsoup/parser/TokeniserState.java =====
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
                    String data = r.consumeData();
                    t.emit(data);
                    break;
            }
        }
