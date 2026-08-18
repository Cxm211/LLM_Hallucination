    public Elements siblingElements() {
        Element parent = parent();
        if (parent == null) {
            return new Elements();
        }
        List<Element> siblings = parent.children();
        Elements result = new Elements();
        for (Element sibling : siblings) {
            if (sibling != this) {
                result.add(sibling);
            }
        }
        return result;
    }