void read(Tokeniser t, CharacterReader r) {
    r.unconsume();
    Token.Comment comment = new Token.Comment();
    comment.data.append(r.consumeTo('>'));
    if (r.current() == '>') {
        r.advance();
    }
    t.emit(comment);
    t.advanceTransition(Data);
}