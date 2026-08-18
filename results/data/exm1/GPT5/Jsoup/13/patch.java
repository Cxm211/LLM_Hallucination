public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);
        
        if (attributeKey.startsWith("abs:")) {
            // if a literal attribute with abs: exists, respect it
            if (attributes.hasKey(attributeKey))
                return true;
            String key = attributeKey.substring(4);
            return !absUrl(key).isEmpty();
        }

        return attributes.hasKey(attributeKey);
    }