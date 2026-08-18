boolean isAppropriateEndTagToken() {
    return lastStartTag != null && tagPending.tagName.equals(lastStartTag.tagName);
}