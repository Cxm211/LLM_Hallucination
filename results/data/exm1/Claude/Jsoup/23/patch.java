String consumeLetterSequence() {
        int start = pos;
        while (!reader.isEmpty()) {
            char c = reader.current();
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
                reader.advance();
            else
                break;
        }

        return reader.consumeToMark();
    }