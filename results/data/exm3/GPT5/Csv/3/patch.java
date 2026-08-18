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
            // If the next char is a meta-character, consume it and return it literally
            if (c == delimiter || c == encapsulator || c == escape) {
                return c;
            }
            // Otherwise, preserve the escape and process the following char normally
            in.unread(c);
            return escape;
        }
    }