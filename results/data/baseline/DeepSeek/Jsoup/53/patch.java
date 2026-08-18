public String chompBalanced(char open, char close) {
    int start = -1;
    int end = -1;
    int depth = 0;
    boolean escaped = false;

    do {
        if (isEmpty()) break;
        Character c = consume();
        if (!escaped) {
            if (c.equals(open)) {
                depth++;
                if (start == -1)
                    start = pos;
            } else if (c.equals(close))
                depth--;
        }
        if (!escaped && c.equals(ESC))
            escaped = true;
        else
            escaped = false;
        if (depth > 0)
            end = pos;
    } while (depth > 0);
    return (end >= 0) ? queue.substring(start, end) : "";
}