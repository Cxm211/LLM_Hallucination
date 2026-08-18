static boolean isSwitchCase(Node n) {
    return n.getType() == Token.CASE || n.getType() == Token.DEFAULT_CASE;
  }