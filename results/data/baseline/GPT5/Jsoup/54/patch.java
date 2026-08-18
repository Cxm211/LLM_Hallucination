private void copyAttributes(org.jsoup.nodes.Node source, Element el) {
    for (Attribute attribute : source.attributes()) {
        String key = attribute.getKey().replaceAll("[^-a-zA-Z0-9_:.]", "");
        if (key == null || key.isEmpty() || !key.matches("^[a-zA-Z_:][-a-zA-Z0-9_:.]*$")) {
            continue;
        }
        el.setAttribute(key, attribute.getValue());
    }
}