    String consumeLetterSequence() {
        int start = pos;
        while (!isEmpty()) {
            char c = input.charAt(pos);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))
                pos++;
            else
                break;
        }

        return input.substring(start, pos);
    }