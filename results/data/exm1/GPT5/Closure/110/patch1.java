  public Node getChildBefore(Node child) {
    if (child == first) {
      return null;
    }
    Node n = first;

    while (n != null && n.next != child) {
      n = n.next;
    }
    return n; // returns null if child is not found or is the first child
  }
