boolean isAppropriateEndTagToken() {
        return lastStartTag != null && tagPending != null && tagPending.tagName.equalsIgnoreCase(lastStartTag.tagName);
    }