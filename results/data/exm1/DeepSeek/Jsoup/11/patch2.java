private Elements matches(boolean own, Elements context) {
    tq.consume(own ? ":matchesOwn" : ":matches");
    String regex = tq.chompBalanced('(', ')');
    Validate.notEmpty(regex, ":matches(regex) query must not be empty");
    Elements result = new Elements();
    for (Element e : context) {
        if (own && e.ownText().matches(regex)) {
            result.add(e);
        } else if (!own && e.text().matches(regex)) {
            result.add(e);
        }
    }
    return result;
}