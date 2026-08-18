public boolean hasAttr(String attributeKey) {
    Validate.notNull(attributeKey);

    if (attributeKey.startsWith("abs:")) {
        String key = attributeKey.substring("abs:".length());
        if (attributes.hasKey(key)) {
            return !absUrl(key).isEmpty();
        }
        return false;
    }
    return attributes.hasKey(attributeKey);
}