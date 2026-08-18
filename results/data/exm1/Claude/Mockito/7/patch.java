private void readTypeVariables() {
    Type actualTypeArgument = getActualTypeArgumentFor(typeVariable);
    if (actualTypeArgument != null) {
        registerTypeVariablesOn(actualTypeArgument);
    }
    for (Type type : typeVariable.getBounds()) {
        registerTypeVariablesOn(type);
    }
}