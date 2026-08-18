private void combinator(char combinator) {
    tq.consumeWhitespace();
    String subQuery = consumeSubQuery();
    
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

private String consumeSubQuery() {
    StringBuilder sq = new StringBuilder();
    while (!tq.isEmpty()) {
        if (tq.matches("["))
            sq.append(tq.chompBalanced('[', ']'));
        else if (tq.matches("("))
            sq.append(tq.chompBalanced('(', ')'));
        else if (tq.matchesAny(combinators))
            break;
        else
            sq.append(tq.consume());
    }
    return sq.toString();
}