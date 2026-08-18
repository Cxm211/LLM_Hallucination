int readEscape() throws IOException {
    // the escape char has just been read (normally a backslash)
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
    case CR:
    case LF:
    case FF: // TODO is this correct?
    case TAB: // TODO is this correct? Do tabs need to be escaped?
    case BACKSPACE: // TODO is this correct?
        return c;
    case END_OF_STREAM:
        throw new IOException("EOF whilst processing escape sequence");
    default:
        // For unrecognized escape sequences, leave the escape in the output
        // and reprocess the following character normally.
        in.unread(c);
        return escape;
    }
}