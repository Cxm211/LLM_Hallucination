private Elements findElements(Elements context) {
    if (tq.matchChomp("#")) {
        return byId();
    } else if (tq.matchChomp(".")) {
        return byClass();
    } else if (tq.matchesWord()) {
        return byTag();
    } else if (tq.matches("[")) {
        return byAttribute();
    } else if (tq.matchChomp("*")) {
        return allElements();
    } else if (tq.matchChomp(":lt(")) {
        return indexLessThan();
    } else if (tq.matchChomp(":gt(")) {
        return indexGreaterThan();
    } else if (tq.matchChomp(":eq(")) {
        return indexEquals();
    } else if (tq.matches(":has(")) {
        return has();
    } else if (tq.matches(":contains(")) {
        return contains(false);
    } else if (tq.matches(":containsOwn(")) {
        return contains(true);
    } else if (tq.matches(":matches(")) {
        return matches(false, context);
    } else if (tq.matches(":matchesOwn(")) {
        return matches(true, context);
    } else {
        throw new SelectorParseException("Could not parse query '%s': unexpected token at '%s'", query, tq.remainder());
    }
}