public int getLength() {
    Object value = getBaseValue();
    return value == null ? 0 : ValueUtils.getLength(value);
}