    private void combinator(char combinator) {
        tq.consumeWhitespace();
        String subQuery = tq.consumeToAny(combinators);
        
        Elements output;
        if (combinator == '>')
            output = filterForChildren(elements, select(subQuery, elements));
        else if (combinator == ' ')
            output = filterForDescendants(elements, select(subQuery, elements));
        else if (combinator == '+')
            output = filterForAdjacentSiblings(elements, select(subQuery, elements));
        else if (combinator == '~')
            output = filterForGeneralSiblings(elements, select(subQuery, elements));
        else
            throw new IllegalStateException("Unknown combinator: " + combinator);
        
        elements.clear(); elements.addAll(output);
    }