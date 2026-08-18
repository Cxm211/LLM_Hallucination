private void readTypeVariables() {
    for (Type type : typeVariable.getBounds()) {
        registerTypeVariablesOn(type);
    }
    Type actualType = getActualTypeArgumentFor(typeVariable);
    if (actualType != null && actualType != typeVariable) {
        registerTypeVariablesOn(actualType);
    }
}