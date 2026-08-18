        void read(Tokeniser t, CharacterReader r) {
            // todo: handle bogus comment starting from eof. when does that trigger?
            // rewind to capture character that lead us here
            r.unconsume();
            String data = r.consumeTo('>');
            if (data.startsWith("?xml")) {
                Token.Doctype doctype = new Token.Doctype();
                doctype.name = "xml";
                t.emit(doctype);
            } else {
                Token.Comment comment = new Token.Comment();
                comment.data.append(data);
                t.emit(comment);
            }
            // todo: replace nullChar with replaceChar
            t.advanceTransition(Data);
        }