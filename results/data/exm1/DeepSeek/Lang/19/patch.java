    public int translate(CharSequence input, int index, Writer out) throws IOException {
        int seqEnd = input.length();
        // Uses -2 to ensure there is something after the &#
        if(input.charAt(index) == '&' && index < seqEnd - 1 && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            // Check that there is at least one char after the &# or &#x
            if(start >= seqEnd) {
                return 0;
            }

            char firstChar = input.charAt(start);
            if(firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;
                if(start >= seqEnd) {
                    return 0;
                }
            }

            int end = start;
            // Note that this supports character codes without a ; on the end
            while(end < seqEnd && input.charAt(end) != ';') 
            {
                end++;
            }

            // If we reached end of input without finding a semicolon, adjust: end is already seqEnd or char at end is ';'
            boolean hasSemicolon = end < seqEnd && input.charAt(end) == ';';

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

            // Return number of characters consumed
            if(hasSemicolon) {
                return end - index + 1;
            } else {
                return end - index;
            }
        }
        return 0;
    }