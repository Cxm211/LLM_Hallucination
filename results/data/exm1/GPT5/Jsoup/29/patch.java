public String title() {
        // title is a preserve whitespace tag (for document output), but normalised here
        Element titleEl = head() != null ? head().getElementsByTag("title").first() : null;
        return titleEl != null ? StringUtil.normaliseWhitespace(titleEl.text()) : "";
    }