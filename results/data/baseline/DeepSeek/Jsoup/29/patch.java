public String title() {
    Element titleEl = getElementsByTag("title").first();
    return titleEl != null ? titleEl.text() : "";
}