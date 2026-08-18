  static boolean isValidDefineValue(Node val, Set<String> defines) {
    switch (val.getType()) {
      case Token.STRING:
      case Token.NUMBER:
      case Token.TRUE:
      case Token.FALSE:
        return true;

      // Binary operators are only valid if both children are valid.
      case Token.ADD:
      case Token.SUB:
      case Token.MUL:
      case Token.DIV:
      case Token.MOD:
      case Token.BITAND:
      case Token.BITOR:
      case Token.BITXOR:
      case Token.LSHIFT:
      case Token.RSHIFT:
      case Token.URSHIFT:
        return isValidDefineValue(val.getFirstChild(), defines)
            && isValidDefineValue(val.getLastChild(), defines);

      // Unary operators are valid if the child is valid.
      case Token.BITNOT:
      case Token.NOT:
      case Token.NEG:
      case Token.POS:
        return isValidDefineValue(val.getFirstChild(), defines);

      // Names are valid if and only if they are defines themselves.
      case Token.NAME:
      case Token.GETPROP:
        if (val.isQualifiedName()) {
          return defines.contains(val.getQualifiedName());
        }
        return false;
    }
    return false;
  }