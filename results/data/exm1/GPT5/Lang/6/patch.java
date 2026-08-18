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
                // Safely write current character(s), handling possible surrogate pairs without relying on toChars for invalid code points
                char ch = input.charAt(pos);
                if (Character.isHighSurrogate(ch) && pos + 1 < len && Character.isLowSurrogate(input.charAt(pos + 1))) {
                    out.write(ch);
                    out.write(input.charAt(pos + 1));
                    pos += 2;
                } else {
                    out.write(ch);
                    pos += 1;
                }
                continue;
            }
            // contract with translators is that they have to understand codepoints 
            // and they just took care of a surrogate pair
            for (int pt = 0; pt < consumed; pt++) {
                pos += Character.charCount(Character.codePointAt(input, pos));
            }
        }
    }