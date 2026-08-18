public boolean hasAttr(String attributeKey) {
    Validate.notNull(attributeKey);

    if (attributeKey.trim().isEmpty()) {
        return false;
    }

    return attributes.hasKey(attributeKey.toLowerCase());
}