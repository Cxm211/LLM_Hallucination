public String title() {
    // title is a preserve whitespace tag (for document output), but normalised here
    Element titleEl = getElementsByTag("title").first();
    if (titleEl != null) {
        return StringUtil.normaliseWhitespace(titleEl.text()).trim();
    }
    return "";
}