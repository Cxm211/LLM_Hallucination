public final void translate(CharSequence input, Writer out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (input == null) {
            return;
        }
        int pos = 0;
        final int len = input.length();
        while (pos < len) {
            int consumed = translate(input, pos, out);
            if (consumed == 0) {
                // Safely write either a single char or a surrogate pair without using toChars on invalid code points
                char c1 = input.charAt(pos);
                if (Character.isHighSurrogate(c1) && pos + 1 < len) {
                    char c2 = input.charAt(pos + 1);
                    if (Character.isLowSurrogate(c2)) {
                        out.write(new char[] { c1, c2 });
                        pos += 2;
                        continue;
                    }
                }
                // Not a valid surrogate pair; write single char
                out.write(c1);
                pos += 1;
            } else {
                // advance by the number of code points consumed
                for (int pt = 0; pt < consumed; pt++) {
                    pos += Character.charCount(Character.codePointAt(input, pos));
                }
            }
        }
    }