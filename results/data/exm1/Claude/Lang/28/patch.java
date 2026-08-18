public int translate(CharSequence input, int index, Writer out) throws IOException {
        if(input.charAt(index) == '&' && index < input.length() - 1 && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            if(start >= input.length()) {
                return 0;
            }

            char firstChar = input.charAt(start);
            if(firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;
                if(start >= input.length()) {
                    return 0;
                }
            }

            int end = start;
            while(end < input.length() && input.charAt(end) != ';') {
                end++;
            }

            if(end >= input.length()) {
                return 0;
            }

            int entityValue;
            try {
                if(isHex) {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 16);
                } else {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 10);
                }
            } catch(NumberFormatException nfe) {
                return 0;
            }

            if(entityValue > 0xFFFF) {
                char[] chars = Character.toChars(entityValue);
                out.write(chars[0]);
                out.write(chars[1]);
            } else {
                out.write(entityValue);
            }
            return 2 + (end - start) + (isHex ? 1 : 0) + 1;
        }
        return 0;
    }