public List<Node> siblingNodes() {
        List<Node> siblings = new ArrayList<Node>();
        if (parentNode == null)
            return siblings;
        for (Node child : parentNode.childNodes()) {
            if (child != this)
                siblings.add(child);
        }
        return siblings;
    }