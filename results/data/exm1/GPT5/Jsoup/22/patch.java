public Elements siblingElements() {
        Elements elements = new Elements();
        Element parent = parent();
        if (parent == null)
            return elements;
        List<Element> siblings = parent.children();
        for (Element el : siblings) {
            if (el != this)
                elements.add(el);
        }
        return elements;
    }