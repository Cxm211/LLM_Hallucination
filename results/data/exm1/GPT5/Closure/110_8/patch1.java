  public Node getChildBefore(Node child) {
    if (child == first) {
      return null;
    }
    // If child is null, return the last child (used when inserting before null to append at end).
    if (child == null) {
      if (first == null) {
        return null;
      }
      Node n = first;
      while (n.next != null) {
        n = n.next;
      }
      return n;
    }
    Node n = first;

    while (n.next != child) {
      n = n.next;
      if (n == null) {
        throw new RuntimeException("node is not a child");
      }
    }
    return n;
  }