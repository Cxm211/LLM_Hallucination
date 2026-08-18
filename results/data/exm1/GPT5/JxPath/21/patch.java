public int getLength() {
        Object base = getBaseValue();
        if (base == null) {
            return 0;
        }
        return ValueUtils.getLength(base);
    }