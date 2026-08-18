void read(Tokeniser t, CharacterReader r) {
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
                    
                case '>':
                    t.emitTagPending();
                    t.transition(Data);
                    break;
                case nullChar: 
                    t.tagPending.appendTagName(replacementStr);
                    break;
                case eof: 
                    t.eofError(this);
                    t.transition(Data);
                    break;
                default: 
                    t.tagPending.appendTagName(c);
            }
        }