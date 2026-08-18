public int translate(CharSequence input, int index, Writer out) throws IOException {
        int seqEnd = input.length();
        if (input.charAt(index) == '&' && index < seqEnd - 1 && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            if (start >= seqEnd) {
                return 0; // nothing after &#
            }

            char firstChar = input.charAt(start);
            if (firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;
            }

            int end = start;
            // collect digits (hex or decimal); semicolon is optional
            if (isHex) {
                while (end < seqEnd) {
                    char c = input.charAt(end);
                    if (c == ';') {
                        break;
                    }
                    if (Character.digit(c, 16) == -1) {
                        break;
                    }
                    end++;
                }
            } else {
                while (end < seqEnd) {
                    char c = input.charAt(end);
                    if (c == ';') {
                        break;
                    }
                    if (c < '0' || c > '9') {
                        break;
                    }
                    end++;
                }
            }

            if (end == start) {
                // no digits collected
                return 0;
            }

            int entityValue;
            try {
                if (isHex) {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 16);
                } else {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 10);
                }
            } catch (NumberFormatException nfe) {
                return 0;
            }

            if (entityValue > 0xFFFF) {
                char[] chrs = Character.toChars(entityValue);
                out.write(chrs[0]);
                out.write(chrs[1]);
            } else {
                out.write(entityValue);
            }

            boolean hasSemicolon = (end < seqEnd && input.charAt(end) == ';');
            return 2 + (isHex ? 1 : 0) + (end - start) + (hasSemicolon ? 1 : 0);
        }
        return 0;
    }