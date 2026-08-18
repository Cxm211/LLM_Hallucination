    private static Elements filterForSelf(Collection<Element> parents, Collection<Element> candidates) {
        Elements children = new Elements();
        CHILD: for (Element c : candidates) {
            for (Element p : parents) {
                if (c.equals(p)) {
                    children.add(c);
                    continue CHILD;
                }
            }   
        }
        return children;
    }