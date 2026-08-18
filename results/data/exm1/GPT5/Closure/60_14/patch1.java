static TernaryValue getPureBooleanValue(Node n) {
    switch (n.getType()) {
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
        // The result of a void expression is always undefined (falsy),
        // but if its operand may have side effects, we cannot treat it as
        // a pure value.
        if (n.hasChildren() && mayHaveSideEffects(n.getFirstChild())) {
          return TernaryValue.UNKNOWN;
        }
        return TernaryValue.FALSE;

      case Token.NAME:
        String name = n.getString();
        if ("undefined".equals(name)
            || "NaN".equals(name)) {
          // We assume here that programs don't change the value of the keyword
          // undefined to something other than the value undefined.
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
        if (!mayHaveSideEffects(n)) {
          return TernaryValue.TRUE;
        }
        break;
    }

    return TernaryValue.UNKNOWN;
  }