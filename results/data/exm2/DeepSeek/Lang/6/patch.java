public final void translate(CharSequence input, Writer out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (input == null) {
            return;
        }
        int pos = 0;
        int len = input.length();
        while (pos < len) {
            int codePoint = Character.codePointAt(input, pos);
            int charCount = Character.charCount(codePoint);
            char[] c = Character.toChars(codePoint);
            out.write(c);
            pos += charCount;
        }
    }