    public int translate(CharSequence input, int index, Writer out) throws IOException {
        int length = input.length();
        if (index + 1 >= length) {
            return 0;
        }
        char first = input.charAt(index);
        char second = input.charAt(index + 1);
        if (first != '&' || second != '#') {
            return 0;
        }
        int start = index + 2;
        if (start >= length) {
            return 0;
        }
        boolean isHex = false;
        char firstChar = input.charAt(start);
        if (firstChar == 'x' || firstChar == 'X') {
            isHex = true;
            start++;
            if (start >= length) {
                return 0;
            }
        }
        int end = start;
        while (end < length && input.charAt(end) != ';') {
            end++;
        }
        if (end >= length || input.charAt(end) != ';') {
            return 0;
        }
        String number = input.subSequence(start, end).toString();
        if (number.isEmpty()) {
            return 0;
        }
        int entityValue;
        try {
            if (isHex) {
                entityValue = Integer.parseInt(number, 16);
            } else {
                entityValue = Integer.parseInt(number, 10);
            }
        } catch (NumberFormatException nfe) {
            return 0;
        }
        if (!Character.isValidCodePoint(entityValue)) {
            return 0;
        }
        char[] chars = Character.toChars(entityValue);
        out.write(chars);
        return end + 1 - index;
    }