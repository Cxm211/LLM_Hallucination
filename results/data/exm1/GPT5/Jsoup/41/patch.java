public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Element element = (Element) o;
        return this.outerHtml().equals(element.outerHtml());
    }