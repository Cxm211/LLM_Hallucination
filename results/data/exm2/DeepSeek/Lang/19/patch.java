public int translate(CharSequence input, int index, Writer out) throws IOException {
    int seqEnd = input.length();
    // Uses -2 to ensure there is something after the &#
    if(input.charAt(index) == '&' && index < seqEnd - 1 && input.charAt(index + 1) == '#') {
        int start = index + 2;
        if (start >= seqEnd) {
            return 0; // Incomplete entity
        }
        boolean isHex = false;
        char firstChar = input.charAt(start);
        if(firstChar == 'x' || firstChar == 'X') {
            start++;
            isHex = true;
            // Check there's more than just an x after the &#
            if (start >= seqEnd) {
                return 0; // No digits after "x"
            }
        }
        int end = start;
        // Note that this supports character codes without a ; on the end
        while (end < seqEnd && Character.digit(input.charAt(end), isHex ? 16 : 10) >= 0) {
            end++;
        }
        boolean hasSemicolon = false;
        if (end < seqEnd && input.charAt(end) == ';') {
            hasSemicolon = true;
        }
        int entityValue;
        try {
            if(isHex) {
                entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 16);
            } else {
                entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 10);
            }
        } catch(NumberFormatException nfe) {
            System.err.println("FAIL: " + input.subSequence(start, end) + "[" + start +"]["+ end +"]");
            return 0;
        }
        if(entityValue > 0xFFFF) {
            char[] chrs = Character.toChars(entityValue);
            out.write(chrs[0]);
            out.write(chrs[1]);
        } else {
            out.write(entityValue);
        }
        int consumed = 2; // for "&#"
        if (isHex) {
            consumed++;
        }
        consumed += (end - start);
        if (hasSemicolon) {
            consumed++;
        }
        return consumed;
    }
    return 0;
}