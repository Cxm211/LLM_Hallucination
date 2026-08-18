  public static Node tryCatch(Node tryBody, Node catchNode) {
    Preconditions.checkState(tryBody.isBlock());
    Preconditions.checkState(catchNode.isCatch());
    Node finallyBody = block().copyInformationFrom(catchNode);
    return new Node(Token.TRY, tryBody, catchNode, finallyBody);
  }