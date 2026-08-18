    public Element clone() {
        Element clone = (Element) super.clone();
        clone.attributes = this.attributes.clone();
        return clone;
    }