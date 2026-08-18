public Whitelist addTags(String... tags) {
        Validate.notNull(tags);

        for (String tagName : tags) {
            Validate.notEmpty(tagName);
            tagNames.add(TagName.valueOf(tagName));
        }
        return this;
    }