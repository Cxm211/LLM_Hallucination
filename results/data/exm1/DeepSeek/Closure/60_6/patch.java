static TernaryValue getImpureBooleanValue(Node n) {
    switch (n.getType()) {
      case Token.ASSIGN:
      case Token.COMMA:
        return getImpureBooleanValue(n.getLastChild());
      case Token.NOT:
        TernaryValue value = getImpureBooleanValue(n.getLastChild());
        return value.not();
      case Token.AND: {
        TernaryValue lhs = getImpureBooleanValue(n.getFirstChild());
        if (lhs == TernaryValue.FALSE) {
          return TernaryValue.FALSE;
        } else {
          TernaryValue rhs = getImpureBooleanValue(n.getLastChild());
          return lhs.and(rhs);
        }
      }
      case Token.OR:  {
        TernaryValue lhs = getImpureBooleanValue(n.getFirstChild());
        if (lhs == TernaryValue.TRUE) {
          return TernaryValue.TRUE;
        } else {
          TernaryValue rhs = getImpureBooleanValue(n.getLastChild());
          return lhs.or(rhs);
        }
      }
      case Token.HOOK:  {
        TernaryValue condition = getImpureBooleanValue(n.getFirstChild());
        Node trueNode = n.getFirstChild().getNext();
        Node falseNode = n.getLastChild();
        if (condition == TernaryValue.TRUE) {
          return getImpureBooleanValue(trueNode);
        } else if (condition == TernaryValue.FALSE) {
          return getImpureBooleanValue(falseNode);
        } else {
          TernaryValue trueValue = getImpureBooleanValue(trueNode);
          TernaryValue falseValue = getImpureBooleanValue(falseNode);
          if (trueValue.equals(falseValue)) {
            return trueValue;
          } else {
            return TernaryValue.UNKNOWN;
          }
        }
      }
      case Token.ARRAYLIT:
      case Token.OBJECTLIT:
        return TernaryValue.TRUE;
      default:
        return getPureBooleanValue(n);
    }
  }