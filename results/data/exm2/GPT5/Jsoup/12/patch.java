private void combinator(char combinator) {
        tq.consumeWhitespace();
        char[] cs = combinators;
        int count = 0;
        for (char c : cs) if (c != '|') count++;
        char[] noPipe = new char[count];
        int i = 0;
        for (char c : cs) if (c != '|') noPipe[i++] = c;
        String subQuery = tq.consumeToAny(noPipe);
        
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