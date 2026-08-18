public static Node tryFinally(Node tryBody, Node finallyBody) {
    Preconditions.checkState(tryBody.isBlock());
    Preconditions.checkState(finallyBody.isBlock());
    Node catchBody = new Node(Token.BLOCK).copyInformationFrom(tryBody);
    return new Node(Token.TRY, tryBody, catchBody, finallyBody);
  }