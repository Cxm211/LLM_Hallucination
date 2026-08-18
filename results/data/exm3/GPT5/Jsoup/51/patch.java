boolean matchesLetter() {
        if (isEmpty())
            return false;
        int cp = Character.codePointAt(input, pos, input.length);
        return Character.isLetter(cp);
    }