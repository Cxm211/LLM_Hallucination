private void readTypeVariables() {
    for (Type type : typeVariable.getBounds()) {
        registerTypeVariablesOn(type);
    }
    Type actual = getActualTypeArgumentFor(typeVariable);
    if (actual != null) {
        registerTypeVariablesOn(actual);
    }
}