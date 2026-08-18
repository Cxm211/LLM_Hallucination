static TernaryValue getPureBooleanValue(Node n) {
    switch (n.getType()) {
      case Token.ASSIGN:
      case Token.COMMA:
        return getPureBooleanValue(n.getLastChild());
      case Token.STRING:
        return TernaryValue.forBoolean(n.getString().length() > 0);

      case Token.NUMBER:
        return TernaryValue.forBoolean(n.getDouble() != 0);

      case Token.NOT:
        return getPureBooleanValue(n.getLastChild()).not();

      case Token.NULL:
      case Token.FALSE:
        return TernaryValue.FALSE;

      case Token.VOID:
          return TernaryValue.FALSE;

      case Token.NAME:
        String name = n.getString();
        if ("undefined".equals(name)
            || "NaN".equals(name)) {
          return TernaryValue.FALSE;
        } else if ("Infinity".equals(name)) {
          return TernaryValue.TRUE;
        }
        break;

      case Token.TRUE:
      case Token.REGEXP:
        return TernaryValue.TRUE;

      case Token.ARRAYLIT:
      case Token.OBJECTLIT:
        return TernaryValue.TRUE;
    }

    return TernaryValue.UNKNOWN;
  }