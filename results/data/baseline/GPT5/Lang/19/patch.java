public int translate(CharSequence input, int index, Writer out) throws IOException {
        int seqEnd = input.length();
        if (index >= seqEnd) {
            return 0;
        }
        // Uses -2 to ensure there is something after the &#
        if (input.charAt(index) == '&' && index < seqEnd - 1 && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            if (start >= seqEnd) {
                return 0;
            }

            char firstChar = input.charAt(start);
            if (firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;
                // Check there's more than just an x after the &#
                if (start >= seqEnd) {
                    return 0;
                }
            }

            int end = start;
            // Support character codes without a ; on the end and stop on non-digit
            while (end < seqEnd) {
                char c = input.charAt(end);
                if (c == ';') {
                    break;
                }
                int d = Character.digit(c, isHex ? 16 : 10);
                if (d == -1) {
                    break;
                }
                end++;
            }

            if (end == start) {
                return 0; // no digits
            }

            boolean hasSemicolon = (end < seqEnd && input.charAt(end) == ';');

            int entityValue;
            try {
                entityValue = Integer.parseInt(input.subSequence(start, end).toString(), isHex ? 16 : 10);
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

            return 2 + (end - start) + (isHex ? 1 : 0) + (hasSemicolon ? 1 : 0);
        }
        return 0;
    }