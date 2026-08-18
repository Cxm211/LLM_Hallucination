private void readTypeVariables() {
    Type actual = getActualTypeArgumentFor(typeVariable);
    if (actual != null) {
        registerTypeVariablesOn(actual);
    }
    for (Type type : typeVariable.getBounds()) {
        registerTypeVariablesOn(type);
    }
}