    public int getLength() {
        Object baseValue = getBaseValue();
        if (baseValue == null) {
            return 0;
        }
        return ValueUtils.getLength(baseValue);
    }