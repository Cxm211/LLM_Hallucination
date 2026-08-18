    Evaluator parse() {
        tq.consumeWhitespace();

        if (tq.matchesAny(combinators)) { // if starts with a combinator, use root as elements
            evals.add(new StructuralEvaluator.Root());
            combinator(tq.consume());
        } else {
            findElements();
        }

        while (!tq.isEmpty()) {
            // hierarchy and extras
            boolean seenWhite = tq.consumeWhitespace();

            if (tq.matchChomp(",")) {
                Evaluator currentEval;
                if (evals.size() == 1) {
                    currentEval = evals.get(0);
                } else {
                    currentEval = new CombiningEvaluator.And(evals);
                }
                java.util.List<Evaluator> list = new java.util.ArrayList<Evaluator>();
                list.add(currentEval);
                CombiningEvaluator.Or or = new CombiningEvaluator.Or(list);
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
            } else { // E.class, E#id, E[attr] etc. AND
                findElements(); // take next el, #. etc off queue
            }
        }

        if (evals.size() == 1)
            return evals.get(0);

        return new CombiningEvaluator.And(evals);
    }