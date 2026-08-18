boolean isAppropriateEndTagToken() {
        return lastStartTag != null && tagPending != null && tagPending.tagName.equals(lastStartTag.tagName);
    }