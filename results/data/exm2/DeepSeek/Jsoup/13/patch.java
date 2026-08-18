    public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.startsWith("abs:")) {
            return !absUrl(attributeKey.substring(4)).isEmpty();
        }
        return attributes.hasKey(attributeKey);
    }