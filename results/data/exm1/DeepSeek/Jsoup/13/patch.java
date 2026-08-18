public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.startsWith("abs:")) {
            String key = attributeKey.substring(4);
            if (attributes.hasKey(key)) {
                String val = absUrl(key);
                if (val.length() > 0) {
                    return true;
                }
            }
        }

        return attributes.hasKey(attributeKey);
    }