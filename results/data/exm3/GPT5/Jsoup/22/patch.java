public Elements siblingElements() {

        Elements els = new Elements();
        Element parent = parent();
        if (parent == null)
            return els;
        List<Element> siblings = parent.children();
        for (Element el : siblings) {
            if (el != this)
                els.add(el);
        }
        return els;
    }