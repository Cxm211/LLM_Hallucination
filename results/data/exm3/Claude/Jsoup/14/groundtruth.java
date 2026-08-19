// ===== FIXED org.jsoup.parser.Tokeniser :: isAppropriateEndTagToken() [lines 193-195] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-14-fixed/src/main/java/org/jsoup/parser/Tokeniser.java =====
    boolean isAppropriateEndTagToken() {
        return tagPending.tagName.equals(lastStartTag.tagName);
    }

// ===== FIXED org.jsoup.parser.TokeniserState :: read(Tokeniser, CharacterReader) [lines 9-29] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-14-fixed/src/main/java/org/jsoup/parser/TokeniserState.java =====
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
