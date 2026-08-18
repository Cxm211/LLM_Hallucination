public int translate(CharSequence input, int index, Writer out) throws IOException {
    int seqEnd = input.length();
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

            if (start >= seqEnd) {
                return 0;
            }
        }

        int end = start;
        int radix = isHex ? 16 : 10;
        while (end < seqEnd) {
            char ch = input.charAt(end);
            if (Character.digit(ch, radix) == -1) {
                break;
            }
            end++;
        }

        if (end == start) {
            return 0;
        }

        boolean semiColon = (end < seqEnd && input.charAt(end) == ';');

        int entityValue;
        try {
            entityValue = Integer.parseInt(input.subSequence(start, end).toString(), radix);
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

        return 2 + (isHex ? 1 : 0) + (end - start) + (semiColon ? 1 : 0);
    }
    return 0;
}