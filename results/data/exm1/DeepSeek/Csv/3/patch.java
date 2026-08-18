int readEscape() throws IOException {
    final int c = in.read();
    switch (c) {
    case 'r':
        return CR;
    case 'n':
        return LF;
    case 't':
        return TAB;
    case 'b':
        return BACKSPACE;
    case 'f':
        return FF;
    case END_OF_STREAM:
        throw new IOException("EOF whilst processing escape sequence");
    default:
        if (c == escape) {
            return escape;
        }
        in.unread(c);
        return escape;
    }
}