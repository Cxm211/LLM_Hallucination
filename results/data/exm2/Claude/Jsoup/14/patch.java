boolean isAppropriateEndTagToken() {
    if (lastStartTag == null) return false;
    return tagPending.tagName.equals(lastStartTag.tagName);
}