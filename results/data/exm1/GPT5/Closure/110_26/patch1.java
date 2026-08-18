  public Node getChildBefore(Node child) {
    if (child == first) {
      return null;
    }
    Node n = first;
    // Safely traverse the sibling list, handling empty-child lists gracefully.
    while (n != null && n.next != child) {
      n = n.next;
    }
    if (n == null) {
      throw new RuntimeException("node is not a child");
    }
    return n;
  }
