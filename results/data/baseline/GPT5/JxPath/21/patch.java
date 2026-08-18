public int getLength() {
        Object base = getBaseValue();
        return base == null ? 0 : ValueUtils.getLength(base);
    }