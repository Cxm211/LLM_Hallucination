public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.startsWith("abs:")) {
            String key = attributeKey.substring(4);
            if (key.length() == 0)
                return false;
            if (!attributes.hasKey(key))
                return false;
            return !absUrl(key).isEmpty();
        }
        return attributes.hasKey(attributeKey);
    }