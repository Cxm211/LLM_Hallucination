void read(Tokeniser t, CharacterReader r) {
    Token.Comment comment = new Token.Comment();
    comment.data.append(r.consumeTo('>'));
    t.emit(comment);
    t.advanceTransition(Data);
}