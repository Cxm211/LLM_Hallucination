// ===== FIXED org.jsoup.nodes.Element :: siblingElements() [lines 433-443] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-22-fixed/src/main/java/org/jsoup/nodes/Element.java =====
    public Elements siblingElements() {
        if (parentNode == null)
            return new Elements(0);

        List<Element> elements = parent().children();
        Elements siblings = new Elements(elements.size() - 1);
        for (Element el: elements)
            if (el != this)
                siblings.add(el);
        return siblings;
    }

// ===== FIXED org.jsoup.nodes.Node :: siblingNodes() [lines 443-453] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-22-fixed/src/main/java/org/jsoup/nodes/Node.java =====
    public List<Node> siblingNodes() {
        if (parentNode == null)
            return Collections.emptyList();

        List<Node> nodes = parentNode.childNodes;
        List<Node> siblings = new ArrayList<Node>(nodes.size() - 1);
        for (Node node: nodes)
            if (node != this)
                siblings.add(node);
        return siblings;
    }

// ===== FIXED org.jsoup.select.Elements :: Elements [lines 18-20] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-22-fixed/src/main/java/org/jsoup/select/Elements.java =====
    public Elements() {
        contents = new ArrayList<Element>();
    }
