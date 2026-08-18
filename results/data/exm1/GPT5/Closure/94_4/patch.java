static boolean isValidDefineValue(Node val, Set<String> defines) {
    switch (val.getType()) {
      case Token.STRING:
      case Token.NUMBER:
      case Token.TRUE:
      case Token.FALSE:
        return true;

      // Binary operators are only valid if both children are valid.
      case Token.BITAND:
      case Token.BITOR:
      case Token.BITXOR: {
        Node c1 = val.getFirstChild();
        Node c2 = (c1 != null) ? c1.getNext() : null;
        return c1 != null && c2 != null &&
            isValidDefineValue(c1, defines) &&
            isValidDefineValue(c2, defines);
      }

      // Unary operators are valid if the child is valid.
      case Token.BITNOT:
      case Token.NOT:
      case Token.NEG:
        return isValidDefineValue(val.getFirstChild(), defines);

      // Names are valid if and only if they are defines themselves.
      case Token.NAME:
      case Token.GETPROP:
        if (val.isQualifiedName()) {
          return defines.contains(val.getQualifiedName());
        }
    }
    return false;
  }