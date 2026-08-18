Attributes getEnforcedAttributes(String tagName) {
        Attributes attrs = new Attributes();
        TagName tag = TagName.valueOf(tagName);
        if (enforcedAttributes.containsKey(tag)) {
            Map<AttributeKey, AttributeValue> keyVals = enforcedAttributes.get(tag);
            for (Map.Entry<AttributeKey, AttributeValue> entry : keyVals.entrySet()) {
                attrs.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return attrs;
    }