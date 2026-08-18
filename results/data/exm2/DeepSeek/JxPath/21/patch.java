public int getLength() {
    Object value = getBaseValue();
    if (value == null) {
        return 0;
    }
    return ValueUtils.getLength(value);
}