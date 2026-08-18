// ===== FIXED org.jsoup.nodes.Element :: indexInList(Element, List) [lines 568-578] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-43-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    private static <E extends Element> Integer indexInList(Element search, List<E> elements) {
        Validate.notNull(search);
        Validate.notNull(elements);

        for (int i = 0; i < elements.size(); i++) {
            E element = elements.get(i);
            if (element == search)
                return i;
        }
        return null;
    }
