  public Node getChildBefore(Node child) {
    if (child == null || first == null) {
      throw new RuntimeException("node is not a child");
    }
    if (child == first) {
      return null;
    }
    Node n = first;

    while (n != null && n.next != child) {
      n = n.next;
    }
    if (n == null) {
      throw new RuntimeException("node is not a child");
    }
    return n;
  }