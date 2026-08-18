    public int translate(CharSequence input, int index, Writer out) throws IOException {
        // TODO: Protect from ArrayIndexOutOfBounds
        if (index < 0 || index >= input.length()) {
            return 0;
        }
        if (input.charAt(index) != '&') {
            return 0;
        }
        if (index + 1 >= input.length() || input.charAt(index + 1) != '#') {
            return 0;
        }
        int start = index + 2;
        if (start >= input.length()) {
            return 0;
        }
        boolean isHex = false;
        char firstChar = input.charAt(start);
        if (firstChar == 'x' || firstChar == 'X') {
            start++;
            isHex = true;
            if (start >= input.length()) {
                return 0;
            }
        }
        int end = start;
        while (end < input.length() && input.charAt(end) != ';') {
            end++;
        }
        if (end >= input.length() || input.charAt(end) != ';') {
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
        if (Character.isBmpCodePoint(entityValue)) {
            out.write(entityValue);
        } else {
            char[] chars = Character.toChars(entityValue);
            out.write(chars);
        }
        return 2 + (end - start) + (isHex ? 1 : 0) + 1;
    }