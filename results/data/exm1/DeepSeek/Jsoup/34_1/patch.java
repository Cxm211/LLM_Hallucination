    int nextIndexOf(CharSequence seq) {
        // doesn't handle scanning for surrogates
        String inputString = new String(input, pos, length - pos);
        return inputString.indexOf(seq.toString());
    }