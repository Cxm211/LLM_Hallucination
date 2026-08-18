public double getFunctionValue() {
    if (optimizer == null) {
        throw new IllegalStateException("no result available");
    }
    return optimizer.getFunctionValue();
}