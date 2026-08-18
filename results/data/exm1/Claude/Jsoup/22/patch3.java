public List<Node> siblingNodes() {
        if (parent() == null)
            return Collections.emptyList();
        return parent().childNodes();
    }