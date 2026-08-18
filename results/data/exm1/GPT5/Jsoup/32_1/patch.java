public Element clone() {
        Element clone = (Element) super.clone();
        // Ensure the cloned element does not share the cached classNames Set with the original
        clone.classNames = null;
        return clone;
    }