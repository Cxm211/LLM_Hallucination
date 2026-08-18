void read(Tokeniser t, CharacterReader r) {
            // todo: handle bogus comment starting from eof. when does that trigger?
            // rewind to capture character that lead us here
            r.unconsume();
            Token.Comment comment = new Token.Comment();
            comment.data.append(r.consumeTo('>'));
            // consume the closing '>' so it doesn't leak into the DOM as text
            if (!r.isEmpty()) r.consume();
            // todo: replace nullChar with replaceChar
            t.emit(comment);
            t.advanceTransition(Data);
        }