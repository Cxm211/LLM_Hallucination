private Elements notPseudo() {
        tq.consume(":not");
        String subQuery = tq.chompBalanced('(', ')');
        Validate.notEmpty(subQuery, ":not(selector) query must not be empty");

        Elements all = root.getAllElements();
        Elements toRemove = select(subQuery, root);
        all.removeAll(toRemove);
        return all;
    }