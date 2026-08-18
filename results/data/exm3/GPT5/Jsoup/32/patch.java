public Element clone() {
        Element clone = (Element) super.clone();
        // ensure the class names set is not shared between the original and the clone
        if (this.classNames != null) {
            clone.classNames = new java.util.LinkedHashSet<>(this.classNames);
        } else {
            clone.classNames = null;
        }
        return clone;
    }