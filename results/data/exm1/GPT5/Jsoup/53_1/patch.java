public String chompBalanced(char open, char close) {
        int start = -1;
        int end = -1;
        int depth = 0;
        char last = 0;
        char lastlast = 0;

        do {
            if (isEmpty()) break;
            Character cObj = consume();
            if (cObj == null) break;
            char c = cObj.charValue();

            boolean isEscaped = (last == ESC && lastlast != ESC);
            if (!isEscaped) {
                if (c == open) {
                    depth++;
                    if (start == -1)
                        start = pos;
                } else if (c == close) {
                    depth--;
                }
            }

            if (depth > 0)
                end = pos; // don't include the outer match pair in the return
            lastlast = last;
            last = c;
        } while (depth > 0);
        return (end >= 0) ? queue.substring(start, end) : "";
    }