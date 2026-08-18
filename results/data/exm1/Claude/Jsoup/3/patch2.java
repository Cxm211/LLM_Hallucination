public Element wrap(String html) {
        Validate.notEmpty(html);

        Element wrapBody = Parser.parseBodyFragment(html, baseUri).body();
        Elements wrapChildren = wrapBody.children();
        Element wrap = wrapChildren.first();
        if (wrap == null)
            return null;

        Element deepest = getDeepChild(wrap);
        parentNode.replaceChild(this, wrap);
        deepest.addChild(this);

        if (wrapChildren.size() > 1) {
            List<Element> remainders = new ArrayList<Element>();
            for (int i = 1; i < wrapChildren.size(); i++) {
                remainders.add(wrapChildren.get(i));
            }
            for (Element remainder : remainders) {
                remainder.parentNode.removeChild(remainder);
                wrap.appendChild(remainder);
            }
        }
        return this;
    }