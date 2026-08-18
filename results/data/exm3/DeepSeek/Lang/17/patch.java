    public final void translate(CharSequence input, Writer out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (input == null) {
            return;
        }
        int pos = 0;
        int len = Character.codePointCount(input, 0, input.length());
        while (pos < len) {
            int consumed = translate(input, pos, out);
            if (consumed == 0) {
                int charIndex = Character.offsetByCodePoints(input, 0, pos);
                int codePoint = Character.codePointAt(input, charIndex);
                char[] c = Character.toChars(codePoint);
                out.write(c);
                pos++;
            } else {
                pos += consumed;
            }
        }
    }