Evaluator parse() {
    tq.consumeWhitespace();

    if (tq.matchesAny(combinators)) {
        evals.add(new StructuralEvaluator.Root());
        combinator(tq.consume());
    } else {
        findElements();
    }

    while (!tq.isEmpty()) {
        boolean seenWhite = tq.consumeWhitespace();

        if (tq.matchChomp(",")) {
            CombiningEvaluator.Or or = new CombiningEvaluator.Or(evals);
            evals.clear();
            evals.add(or);
            while (!tq.isEmpty()) {
                String subQuery = tq.chompTo(",");
                or.add(parse(subQuery));
            }
        } else if (tq.matchesAny(combinators)) {
            combinator(tq.consume());
        } else if (seenWhite) {
            combinator(' ');
        } else {
            findElements();
        }
    }

    if (evals.size() == 1)
        return evals.get(0);

    return new CombiningEvaluator.And(evals);
}