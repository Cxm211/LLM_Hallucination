void read(Tokeniser t, CharacterReader r) {
    // todo: handle bogus comment starting from eof. when does that trigger?
    // rewind to capture character that lead us here
    r.unconsume();
    Token.Comment comment = new Token.Comment();
    String data = r.consumeTo('>');
    if (data.startsWith("?xml")) {
        t.emit(new Token.Doctype());
    } else {
        comment.data.append(data);
        t.emit(comment);
    }
    // todo: replace nullChar with replaceChar
    t.advanceTransition(Data);
}