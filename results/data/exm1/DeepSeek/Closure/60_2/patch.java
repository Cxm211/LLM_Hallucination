  static TernaryValue getImpureBooleanValue(Node n) {
    switch (n.getType()) {
      case Token.ASSIGN:
      case Token.COMMA:
        // For ASSIGN and COMMA the value is the value of the RHS.
        return getImpureBooleanValue(n.getLastChild());
      case Token.NOT:
        TernaryValue value = getImpureBooleanValue(n.getLastChild());
        return value.not();
      case Token.AND: {
        TernaryValue lhs = getImpureBooleanValue(n.getFirstChild());
        if (lhs == TernaryValue.FALSE) {
          return TernaryValue.FALSE;
        } else if (lhs == TernaryValue.TRUE) {
          return getImpureBooleanValue(n.getLastChild());
        } else {
          return TernaryValue.UNKNOWN;
        }
      }
      case Token.OR:  {
        TernaryValue lhs = getImpureBooleanValue(n.getFirstChild());
        if (lhs == TernaryValue.TRUE) {
          return TernaryValue.TRUE;
        } else if (lhs == TernaryValue.FALSE) {
          return getImpureBooleanValue(n.getLastChild());
        } else {
          return TernaryValue.UNKNOWN;
        }
      }
      case Token.HOOK:  {
        TernaryValue trueValue = getImpureBooleanValue(
            n.getFirstChild().getNext());
        TernaryValue falseValue = getImpureBooleanValue(n.getLastChild());
        if (trueValue.equals(falseValue)) {
          return trueValue;
        } else {
          return TernaryValue.UNKNOWN;
        }
      }
      case Token.ARRAYLIT:
      case Token.OBJECTLIT:
        // ignoring side-effects
        return TernaryValue.TRUE;


      default:
        return getPureBooleanValue(n);
    }
  }