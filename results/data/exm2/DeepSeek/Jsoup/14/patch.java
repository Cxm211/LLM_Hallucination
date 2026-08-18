boolean isAppropriateEndTagToken() {
    return tagPending.tagName.equalsIgnoreCase(lastStartTag.tagName);
}