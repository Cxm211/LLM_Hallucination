// ===== FIXED org.jsoup.select.Selector :: combinator(char) [lines 137-154] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-12-fixed/src/main/java/org/jsoup/select/Selector.java =====
    private void combinator(char combinator) {
        tq.consumeWhitespace();
        String subQuery = consumeSubQuery(); // support multi > childs
        
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
