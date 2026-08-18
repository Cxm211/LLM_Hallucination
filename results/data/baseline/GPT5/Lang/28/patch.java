// buggy code
    public int translate(CharSequence input, int index, Writer out) throws IOException {
        int len = input.length();
        if (index < 0 || index >= len) {
            return 0;
        }
        if (input.charAt(index) == '&' && (index + 1) < len && input.charAt(index + 1) == '#') {
            int start = index + 2;
            if (start >= len) {
                return 0;
            }
            boolean isHex = false;

            char firstChar = input.charAt(start);
            if (firstChar == 'x' || firstChar == 'X') {
                isHex = true;
                start++;
                if (start >= len) {
                    return 0;
                }
            }

            int end = start;
            while (end < len && input.charAt(end) != ';') {
                end++;
            }
            if (end == len) { // no terminating ';'
                return 0;
            }
            if (start == end) { // no digits
                return 0;
            }

            int entityValue;
            try {
                String number = input.subSequence(start, end).toString();
                entityValue = Integer.parseInt(number, isHex ? 16 : 10);
            } catch (NumberFormatException nfe) {
                return 0;
            }

            if (!Character.isValidCodePoint(entityValue)) {
                return 0;
            }

            if (entityValue > 0xFFFF) {
                out.write(Character.toChars(entityValue));
            } else {
                out.write(entityValue);
            }
            return 2 + (end - start) + (isHex ? 1 : 0) + 1;
        }
        return 0;
    }
