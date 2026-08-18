// buggy function
        public String toString() {
            return String.format(":matchesOwn(%s)", pattern);
        }

    private void findElements() {
        if (tq.matchChomp("#"))
            byId();
        else if (tq.matchChomp("."))
            byClass();
        else if (tq.matchesWord() || tq.matches("*|"))
            byTag();
        else if (tq.matches("["))
            byAttribute();
        else if (tq.matchChomp("*"))
            allElements();
        else if (tq.matchChomp(":lt("))
            indexLessThan();
        else if (tq.matchChomp(":gt("))
            indexGreaterThan();
        else if (tq.matchChomp(":eq("))
            indexEquals();
        else if (tq.matches(":has("))
            has();
        else if (tq.matches(":contains("))
            contains(false);
        else if (tq.matches(":containsOwn("))
            contains(true);
        else if (tq.matches(":containsData("))
            containsData();
        else if (tq.matches(":matches("))
            matches(false);
        else if (tq.matches(":matchesOwn("))
            matches(true);
        else if (tq.matches(":not("))
            not();
		else if (tq.matchChomp(":nth-child("))
        	cssNthChild(false, false);
        else if (tq.matchChomp(":nth-last-child("))
        	cssNthChild(true, false);
        else if (tq.matchChomp(":nth-of-type("))
        	cssNthChild(false, true);
        else if (tq.matchChomp(":nth-last-of-type("))
        	cssNthChild(true, true);
        else if (tq.matchChomp(":first-child"))
        	evals.add(new Evaluator.IsFirstChild());
        else if (tq.matchChomp(":last-child"))
        	evals.add(new Evaluator.IsLastChild());
        else if (tq.matchChomp(":first-of-type"))
        	evals.add(new Evaluator.IsFirstOfType());
        else if (tq.matchChomp(":last-of-type"))
        	evals.add(new Evaluator.IsLastOfType());
        else if (tq.matchChomp(":only-child"))
        	evals.add(new Evaluator.IsOnlyChild());
        else if (tq.matchChomp(":only-of-type"))
        	evals.add(new Evaluator.IsOnlyOfType());
        else if (tq.matchChomp(":empty"))
        	evals.add(new Evaluator.IsEmpty());
        else if (tq.matchChomp(":root"))
        	evals.add(new Evaluator.IsRoot());
		else // unhandled
            throw new Selector.SelectorParseException("Could not parse query '%s': unexpected token at '%s'", query, tq.remainder());

    }

// trigger testcase
// org/jsoup/select/SelectorTest.java::splitOnBr
@Test public void splitOnBr() {
        String html = "<div><p>One<br>Two<br>Three</p></div>";
        Document doc = Jsoup.parse(html);

        Elements els = doc.select("p:matchText");
        assertEquals(3, els.size());
        assertEquals("One", els.get(0).text());
        assertEquals("Two", els.get(1).text());
        assertEquals("Three", els.get(2).toString());
    }

// org/jsoup/select/SelectorTest.java::textAsElements
@Test public void textAsElements() {
        String html = "<p>One<br>Two</p>";
        Document doc = Jsoup.parse(html);
        String origHtml = doc.html();

        Elements one = doc.select("p:matchText:first-child");
        assertEquals("One", one.first().text());

        Elements two = doc.select("p:matchText:last-child");
        assertEquals("Two", two.first().text());

        assertEquals(origHtml, doc.html());

        assertEquals("Two", doc.select("p:matchText + br + *").text());
    }
