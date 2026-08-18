    public String title() {
        // title is a preserve whitespace tag (for document output), but normalised here
        Element titleEl = head().getElementsByTag("title").first();
        return titleEl != null ? titleEl.text().trim() : "";
    }