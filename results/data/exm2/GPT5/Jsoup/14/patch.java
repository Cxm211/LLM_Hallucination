    boolean isAppropriateEndTagToken() {
        return tagPending != null && lastStartTag != null && tagPending.tagName != null && lastStartTag.tagName != null && tagPending.tagName.equalsIgnoreCase(lastStartTag.tagName);
    }
