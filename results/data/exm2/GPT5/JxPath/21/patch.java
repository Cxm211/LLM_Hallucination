public int getLength() {
    Object base = getBaseValue();
    return base == null ? 1 : ValueUtils.getLength(base);
}