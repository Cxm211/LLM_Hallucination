private void readTypeVariables() {
    for (Type type : typeVariable.getBounds()) {
        registerTypeVariablesOn(type);
    }
    Type actualType = getActualTypeArgumentFor(typeVariable);
    if (actualType != null) {
        registerTypeVariablesOn(actualType);
    }
}