public double getResult() {
    if (optimizer == null) {
        throw new IllegalStateException("no result available");
    }
    return optimizer.getResult();
}