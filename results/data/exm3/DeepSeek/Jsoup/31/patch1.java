        void read(Tokeniser t, CharacterReader r) {
            // todo: handle bogus comment starting from eof. when does that trigger?
            // rewind to capture character that lead us here
            r.unconsume();
            String data = r.consumeTo('>');
            // Check if this is an XML declaration
            if (data.startsWith("?xml") && data.trim().endsWith("?")) {
                Token.Declaration decl = new Token.Declaration();
                decl.data.append(data);
                t.emit(decl);
            } else {
                Token.Comment comment = new Token.Comment();
                comment.data.append(data);
                t.emit(comment);
            }
            // todo: replace nullChar with replaceChar
            t.advanceTransition(Data);
        }