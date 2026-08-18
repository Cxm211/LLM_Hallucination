public List<Node> siblingNodes() {
        Node p = parent();
        List<Node> result = new ArrayList<Node>();
        if (p == null)
            return result;
        List<Node> siblings = p.childNodes();
        for (Node n : siblings) {
            if (n != this)
                result.add(n);
        }
        return result;
    }