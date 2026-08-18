public String title() {
    Element titleEl = this.getElementsByTag("title").first();
    return titleEl != null ? titleEl.text().trim() : "";
}