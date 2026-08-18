public int getLength() {
    Object baseValue = getBaseValue();
    return baseValue == null ? 0 : ValueUtils.getLength(baseValue);
}