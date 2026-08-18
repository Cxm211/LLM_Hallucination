    /**
     Add a list of allowed attributes to a tag. (If an attribute is not allowed on an element, it will be removed.)
     <p/>
     To make an attribute valid for <b>all tags</b>, use the pseudo tag <code>:all</code>, e.g.
     <code>addAttributes(":all", "class")</code>.

     @param tag  The tag the attributes are for
     @param keys List of valid attributes for the tag
     @return this (for chaining)
     */
    public Whitelist addAttributes(String tag, String... keys) {
        Validate.notEmpty(tag);
        Validate.notNull(keys);

        TagName tagName = TagName.valueOf(tag);
        Set<AttributeKey> attributeSet = new HashSet<AttributeKey>();
        for (String key : keys) {
            Validate.notEmpty(key);
            attributeSet.add(AttributeKey.valueOf(key));
        }
        if (attributes.containsKey(tagName)) {
            Set<AttributeKey> currentSet = attributes.get(tagName);
            currentSet.addAll(attributeSet);
        } else {
            attributes.put(tagName, attributeSet);
        }
        return this;
    }