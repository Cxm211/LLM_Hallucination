public Element clone() {
        Element clone = (Element) super.clone();
        java.util.Set<String> classes = classNames();
        if (classes != null) {
            clone.classNames(new java.util.LinkedHashSet<String>(classes));
        }
        return clone;
    }