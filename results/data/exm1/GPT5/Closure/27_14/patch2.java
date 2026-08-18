public static Node tryCatch(Node tryBody, Node catchNode) {
    Preconditions.checkState(tryBody.isBlock());
    Preconditions.checkState(catchNode.isCatch());
    Node catchBody = new Node(Token.BLOCK).copyInformationFrom(catchNode);
    catchBody.addChildToBack(catchNode);
    return new Node(Token.TRY, tryBody, catchBody);
  }