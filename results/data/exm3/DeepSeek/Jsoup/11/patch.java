    private Elements select() {
        tq.consumeWhitespace();
        
        if (tq.matchesAny(combinators)) { // if starts with a combinator, use root as elements
            elements.add(root);
            combinator(tq.consume().toString());
        } else {
            addElements(findElements()); // chomp first element matcher off queue 
        }            
               
        while (!tq.isEmpty()) {
            // hierarchy and extras
            boolean seenWhite = tq.consumeWhitespace();
            
            if (tq.matchChomp(",")) { // group or
                while (!tq.isEmpty()) {
                    // consume subquery up to next comma that is not inside parentheses, brackets, or quotes
                    StringBuilder subQuery = new StringBuilder();
                    int parenDepth = 0;
                    int bracketDepth = 0;
                    boolean inQuote = false;
                    char quoteChar = 0;
                    while (!tq.isEmpty()) {
                        char c = tq.consume();
                        if (!inQuote) {
                            if (c == '(') {
                                parenDepth++;
                            } else if (c == ')') {
                                parenDepth--;
                            } else if (c == '[') {
                                bracketDepth++;
                            } else if (c == ']') {
                                bracketDepth--;
                            } else if (c == '\'' || c == '"') {
                                inQuote = true;
                                quoteChar = c;
                            }
                        } else {
                            if (c == quoteChar) {
                                inQuote = false;
                            }
                        }
                        if (c == ',' && parenDepth == 0 && bracketDepth == 0 && !inQuote) {
                            break;
                        }
                        subQuery.append(c);
                    }
                    elements.addAll(select(subQuery.toString(), root));
                }
            } else if (tq.matchesAny(combinators)) {
                combinator(tq.consume().toString());
            } else if (seenWhite) {
                combinator(" ");
            } else { // E.class, E#id, E[attr] etc. AND
                Elements candidates = findElements(); // take next el, #. etc off queue
                intersectElements(filterForSelf(elements, candidates));
            }
        }
        return new Elements(elements);
    }