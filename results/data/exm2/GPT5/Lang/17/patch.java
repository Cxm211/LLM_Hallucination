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
        int consumed = translate(input, pos, out);
        if (consumed == 0) {
            int codepoint = Character.codePointAt(input, pos);
            out.write(Character.toChars(codepoint));
            pos += Character.charCount(codepoint);
        } else {
            for (int pt = 0; pt < consumed && pos < len; pt++) {
                int codepoint = Character.codePointAt(input, pos);
                pos += Character.charCount(codepoint);
            }
        }
    }
}