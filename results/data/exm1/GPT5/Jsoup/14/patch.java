boolean isAppropriateEndTagToken() {
        if (tagPending == null || lastStartTag == null)
            return false;
        if (tagPending.tagName == null || lastStartTag.tagName == null)
            return false;
        return tagPending.tagName.equalsIgnoreCase(lastStartTag.tagName);
    }