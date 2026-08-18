        Or(Collection<Evaluator> evaluators) {
            super();
            if (evaluators.size() > 1)
                this.evaluators.add(new And(evaluators));
            else // 0 or 1
                this.evaluators.addAll(evaluators);
        }

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
            } else { // E.class, E#id, E[attr] etc. AND
                findElements(); // take next el, #. etc off queue
            }
        }

        if (evals.size() == 1)
            return evals.get(0);

        return new CombiningEvaluator.And(evals);
    }

    private void combinator(char combinator) {
        tq.consumeWhitespace();
        String subQuery = consumeSubQuery(); // support multi > childs
        Evaluator e;

        if (evals.size() == 1)
            e = evals.get(0);
        else
            e = new CombiningEvaluator.And(evals);
        evals.clear();
        Evaluator f = parse(subQuery);

        if (combinator == '>')
            evals.add(new CombiningEvaluator.And(f, new StructuralEvaluator.ImmediateParent(e)));
        else if (combinator == ' ')
            evals.add(new CombiningEvaluator.And(f, new StructuralEvaluator.Parent(e)));
        else if (combinator == '+')
            evals.add(new CombiningEvaluator.And(f, new StructuralEvaluator.ImmediatePreviousSibling(e)));
        else if (combinator == '~')
            evals.add(new CombiningEvaluator.And(f, new StructuralEvaluator.PreviousSibling(e)));
        else
            throw new Selector.SelectorParseException("Unknown combinator: " + combinator);
    }

// trigger testcase
@Test public void handlesCommasInSelector() {
        Document doc = Jsoup.parse("<p name='1,2'>One</p><div>Two</div><ol><li>123</li><li>Text</li></ol>");

        Elements ps = doc.select("[name=1,2]");
        assertEquals(1, ps.size());

        Elements containers = doc.select("div, li:matches([0-9,]+)");
        assertEquals(2, containers.size());
        assertEquals("div", containers.get(0).tagName());
        assertEquals("li", containers.get(1).tagName());
        assertEquals("123", containers.get(1).text());
    }

@Test public void mixCombinatorGroup() {
        String h = "<div class=foo><ol><li>One<li>Two<li>Three</ol></div>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select(".foo > ol, ol > li + li");

        assertEquals(2, els.size());
        assertEquals("li", els.get(0).tagName());
        assertEquals("Three", els.get(1).text());
    }
