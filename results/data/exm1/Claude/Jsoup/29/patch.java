public String title() {
    Element titleEl = getElementsByTag("title").first();
    return titleEl != null ? StringUtil.normaliseWhitespace(titleEl.text()).trim() : "";
}