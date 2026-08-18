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
        case 'u': {
            int value = 0;
            for (int i = 0; i < 4; i++) {
                int ch = in.read();
                if (ch == END_OF_STREAM) {
                    throw new IOException("EOF whilst processing escape sequence");
                }
                int digit = Character.digit((char) ch, 16);
                if (digit == -1) {
                    throw new IOException("Invalid Unicode escape sequence");
                }
                value = (value << 4) | digit;
            }
            return value;
        }
        case CR:
        case LF:
        case FF: // TODO is this correct?
        case TAB: // TODO is this correct? Do tabs need to be escaped?
        case BACKSPACE: // TODO is this correct?
            return c;
        case END_OF_STREAM:
            throw new IOException("EOF whilst processing escape sequence");
        default:
            // Now check for meta-characters
                return c;
            // indicate unexpected char - available from in.getLastChar()
        }
    }