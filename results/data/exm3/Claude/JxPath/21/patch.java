public int getLength() {
    Object baseValue = getBaseValue();
    if (baseValue == null) {
        return 1;
    }
    return ValueUtils.getLength(baseValue);
}