private Elements select() {
    tq.consumeWhitespace();
    
    if (tq.matchesAny(combinators)) {
        elements.add(root);
        combinator(tq.consume().toString());
    } else {
        addElements(findElements(new Elements(root)));
    }
    Elements searchContext = elements;
    while (!tq.isEmpty()) {
        boolean seenWhite = tq.consumeWhitespace();
        
        if (tq.matchChomp(",")) {
            while (!tq.isEmpty()) {
                String subQuery = tq.chompTo(",");
                elements.addAll(select(subQuery, root));
            }
        } else if (tq.matchesAny(combinators)) {
            combinator(tq.consume().toString());
        } else if (seenWhite) {
            combinator(" ");
        } else {
            Elements candidates = findElements(elements);
            intersectElements(filterForSelf(elements, candidates));
        }
    }
    return new Elements(elements);
}