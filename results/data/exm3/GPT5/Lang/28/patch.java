public int translate(CharSequence input, int index, Writer out) throws IOException {
        int len = input.length();
        if (index < 0 || index >= len) {
            return 0;
        }
        if (input.charAt(index) != '&') {
            return 0;
        }
        if (index + 1 >= len || input.charAt(index + 1) != '#') {
            return 0;
        }

        int start = index + 2;
        boolean isHex = false;

        if (start >= len) {
            return 0;
        }

        char firstChar = input.charAt(start);
        if (firstChar == 'x' || firstChar == 'X') {
            start++;
            isHex = true;
            if (start >= len) {
                return 0;
            }
        }

        int end = start;
        while (end < len && input.charAt(end) != ';') {
            end++;
        }

        if (end >= len) {
            // no terminating semicolon
            return 0;
        }
        if (end == start) {
            // no digits
            return 0;
        }

        int entityValue;
        try {
            entityValue = Integer.parseInt(input.subSequence(start, end).toString(), isHex ? 16 : 10);
        } catch (NumberFormatException nfe) {
            return 0;
        }

        if (entityValue < 0 || entityValue > 0x10FFFF) {
            return 0;
        }

        if (entityValue <= 0xFFFF) {
            out.write(entityValue);
        } else {
            char[] chars = Character.toChars(entityValue);
            out.write(chars[0]);
            out.write(chars[1]);
        }
        return (end - index) + 1;
    }