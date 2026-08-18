private void combinator(char combinator) {
        tq.consumeWhitespace();
        // consume subQuery until the next top-level combinator, respecting quotes and bracket/paren nesting
        StringBuilder sb = new StringBuilder();
        int bracketDepth = 0; // [ ]
        int parenDepth = 0;   // ( )
        boolean inQuote = false;
        char quoteCh = 0;
        while (!tq.isEmpty()) {
            char c = tq.consume();
            if (inQuote) {
                sb.append(c);
                if (c == quoteCh) {
                    inQuote = false;
                    quoteCh = 0;
                }
                continue;
            }
            if (c == '\'' || c == '"') {
                inQuote = true;
                quoteCh = c;
                sb.append(c);
                continue;
            }
            if (c == '[') {
                bracketDepth++;
                sb.append(c);
                continue;
            }
            if (c == ']' && bracketDepth > 0) {
                bracketDepth--;
                sb.append(c);
                continue;
            }
            if (c == '(') {
                parenDepth++;
                sb.append(c);
                continue;
            }
            if (c == ')' && parenDepth > 0) {
                parenDepth--;
                sb.append(c);
                continue;
            }
            // if we're at top level and hit a combinator, step back and stop
            if (bracketDepth == 0 && parenDepth == 0) {
                boolean isComb = false;
                for (char comb : combinators) {
                    if (c == comb) { isComb = true; break; }
                }
                if (isComb) {
                    tq.unconsume();
                    break;
                }
            }
            sb.append(c);
        }
        String subQuery = sb.toString();
        Elements output;
        if (combinator == '>')
            output = filterForChildren(elements, select(subQuery, elements));
        else if (combinator == ' ')
            output = filterForDescendants(elements, select(subQuery, elements));
        else if (combinator == '+')
            output = filterForAdjacentSiblings(elements, select(subQuery, root));
        else if (combinator == '~')
            output = filterForGeneralSiblings(elements, select(subQuery, root));
        else
            throw new IllegalStateException("Unknown combinator: " + combinator);
        elements.clear(); elements.addAll(output);
    }