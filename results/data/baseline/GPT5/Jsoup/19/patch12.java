boolean isSafeAttribute(String tagName, Element el, Attribute attr) {
        TagName tag = TagName.valueOf(tagName);
        AttributeKey key = AttributeKey.valueOf(attr.getKey());

        if (attributes.containsKey(tag) && attributes.get(tag).contains(key)) {
            if (protocols.containsKey(tag)) {
                Map<AttributeKey, Set<Protocol>> attrProts = protocols.get(tag);
                return !attrProts.containsKey(key) || testValidProtocol(el, attr, attrProts.get(key));
            } else {
                return true;
            }
        }

        if (!tagName.equals(":all")) {
            return isSafeAttribute(":all", el, attr);
        }
        return false;
    }