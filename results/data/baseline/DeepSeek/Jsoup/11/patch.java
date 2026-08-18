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
                // Recursively parse the rest of the query as a separate selector
                String remainder = tq.remainder();
                elements.addAll(select(remainder, root));
                break; // exit the loop, as the remainder is handled
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