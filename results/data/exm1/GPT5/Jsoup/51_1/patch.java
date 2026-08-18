boolean matchesLetter() {
        if (isEmpty())
            return false;
        char c = input[pos];
        if (Character.isLetter(c))
            return true;
        if (Character.isHighSurrogate(c) && pos + 1 < input.length) {
            char c2 = input[pos + 1];
            if (Character.isLowSurrogate(c2)) {
                int cp = Character.toCodePoint(c, c2);
                return Character.isLetter(cp);
            }
        }
        return false;
    }