    public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.startsWith("abs:")) {
            return !attr(attributeKey).isEmpty();
        }
        return attributes.hasKey(attributeKey);
    }