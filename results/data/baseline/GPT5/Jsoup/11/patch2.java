    private Elements matches(boolean own) {
        tq.consume(own? ":matchesOwn" : ":matches");
        String regex = tq.chompBalanced('(', ')'); // don't unescape, as regex bits will be escaped
        Validate.notEmpty(regex, ":matches(regex) query must not be empty");
        
        return own ? root.getElementsMatchingOwnText(regex) : root.getElementsMatchingText(regex);
    }