void read(Tokeniser t, CharacterReader r) {
    r.unconsume();
    Token.Comment comment = new Token.Comment();
    comment.data.append(r.consumeTo('>'));
    t.emit(comment);
    t.advanceTransition(Data);
}