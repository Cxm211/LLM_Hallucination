public Element clone() {
        Element clone = (Element) super.clone();
        clone.classNames(this.classNames());
        return clone;
    }