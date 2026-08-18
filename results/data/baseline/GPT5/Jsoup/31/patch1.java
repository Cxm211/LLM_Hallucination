void read(Tokeniser t, CharacterReader r) {
    // rewind to capture character that lead us here
    r.unconsume();
    Token.Comment comment = new Token.Comment();
    String data = r.consumeTo('>');
    comment.data.append(data);
    comment.bogus = true;
    if (!r.isEmpty())
        r.consume(); // consume '>'
    t.emit(comment);
    t.advanceTransition(Data);
}