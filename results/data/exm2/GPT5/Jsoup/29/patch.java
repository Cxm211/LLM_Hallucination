public String title() {
        // title is a preserve whitespace tag (for document output), but normalised here
        Element titleEl = getElementsByTag("title").first();
        return titleEl != null ? org.jsoup.helper.StringUtil.normaliseWhitespace(titleEl.text()) : "";
    }