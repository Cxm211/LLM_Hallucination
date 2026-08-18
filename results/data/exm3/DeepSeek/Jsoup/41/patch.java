    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Element element = (Element) o;

        if (!tag.equals(element.tag)) return false;
        if (!attributes.equals(element.attributes)) return false;
        if (!childNodes.equals(element.childNodes)) return false;
        return true;
    }