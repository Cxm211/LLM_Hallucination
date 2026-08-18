public Elements siblingElements() {
        Elements siblings = new Elements();
        Element p = parent();
        if (p == null)
            return siblings;
        for (Element child : p.children()) {
            if (child != this)
                siblings.add(child);
        }
        return siblings;
    }