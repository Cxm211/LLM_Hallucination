    public void describeTo(Description description) {
        description.appendText("same(");
        if (wanted == null) {
            description.appendText("null");
        } else {
            appendQuoting(description);
            description.appendText(wanted.toString());
            appendQuoting(description);
        }
        description.appendText(")");
    }