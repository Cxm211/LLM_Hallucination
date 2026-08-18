public Elements siblingElements() {
        if (parent() == null)
            return new Elements();
        List<Element> elements = parent().children();
        Elements siblings = new Elements();
        for (Element el : elements) {
            if (el != this)
                siblings.add(el);
        }
        return siblings;
    }