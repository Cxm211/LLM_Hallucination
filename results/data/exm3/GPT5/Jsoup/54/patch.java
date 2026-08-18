private void copyAttributes(org.jsoup.nodes.Node source, Element el) {
    for (Attribute attribute : source.attributes()) {
        // valid xml attribute names are: ^[a-zA-Z_:][-a-zA-Z0-9_:.]*
        String key = attribute.getKey();
        // remove any invalid characters
        key = key.replaceAll("[^-a-zA-Z0-9_:.]", "");
        // ensure the first character is valid
        key = key.replaceAll("^[^a-zA-Z_:]+", "");
        if (key.length() == 0) {
            // skip attributes that cannot be represented in XML
            continue;
        }
        el.setAttribute(key, attribute.getValue());
    }
}