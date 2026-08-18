private Node tryFoldComparison(Node n, Node left, Node right) {
    if (!NodeUtil.isLiteralValue(left, false) ||
        !NodeUtil.isLiteralValue(right, false)) {
      if (n.getType() != Token.GT && n.getType() != Token.LT) {
        return n;
      }
    }

    int op = n.getType();
    boolean result;

    boolean rightLiteral = NodeUtil.isLiteralValue(right, false);
    boolean undefinedRight = ((Token.NAME == right.getType()
          && right.getString().equals("undefined"))
          || (Token.VOID == right.getType()
              && NodeUtil.isLiteralValue(right.getFirstChild(), false)));
    int lhType = left.getType();
    int rhType = right.getType();
    switch (lhType) {
      case Token.VOID:
        if (!NodeUtil.isLiteralValue(left.getFirstChild(), false)) {
          return n;
        } else if (!rightLiteral) {
          return n;
        } else {
          result = compareToUndefined(right, op);
        }
        break;

      case Token.NULL:
      case Token.TRUE:
      case Token.FALSE:
        if (undefinedRight) {
          result = compareToUndefined(left, op);
          break;
        }
        if (rhType != Token.TRUE &&
            rhType != Token.FALSE &&
            rhType != Token.NULL) {
          return n;
        }
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            result = lhType == rhType;
            break;

          case Token.SHNE:
          case Token.NE:
            result = lhType != rhType;
            break;

          case Token.GE:
          case Token.LE:
          case Token.GT:
          case Token.LT:
            Boolean compareResult = compareAsNumbers(op, left, right);
            if (compareResult != null) {
              result = compareResult;
            } else {
              return n;
            }
            break;

          default:
            return n;
        }
        break;

      case Token.THIS:
        if (right.getType() != Token.THIS) {
          return n;
        }
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            result = true;
            break;

          case Token.SHNE:
          case Token.NE:
            result = false;
            break;

          default:
            return n;
        }
        break;

      case Token.STRING:
        if (undefinedRight) {
          result = compareToUndefined(left, op);
          break;
        }
        if (Token.STRING != right.getType()) {
          return n;
        }
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            result = left.getString().equals(right.getString());
            break;

          case Token.SHNE:
          case Token.NE:
            result = !left.getString().equals(right.getString());
            break;

          default:
            return n;
        }
        break;

      case Token.NUMBER:
        if (undefinedRight) {
          result = compareToUndefined(left, op);
          break;
        }
        if (Token.NUMBER != right.getType()) {
          return n;
        }
        switch (op) {
          case Token.SHEQ:
          case Token.EQ:
            result = left.getDouble() == right.getDouble();
            break;
          case Token.SHNE:
          case Token.NE:
            result = left.getDouble() != right.getDouble();
            break;
          case Token.GE:
          case Token.LE:
          case Token.GT:
          case Token.LT:
            Boolean compareResult = compareAsNumbers(op, left, right);
            if (compareResult != null) {
              result = compareResult;
            } else {
              return n;
            }
            break;
          default:
            return n;
        }
        break;

      case Token.NAME:
        if (undefinedRight) {
          result = compareToUndefined(left, op);
          break;
        }

        if (rightLiteral) {
          boolean undefinedLeft = (left.getString().equals("undefined"));
          if (undefinedLeft) {
            result = compareToUndefined(right, op);
            break;
          }
        }

        if (Token.NAME != right.getType()) {
          return n;
        }
        String ln = left.getString();
        String rn = right.getString();
        if (!ln.equals(rn)) {
          return n;
        }

        switch (op) {
          case Token.LT:
          case Token.GT:
            result = false;
            break;
          default:
            return n;
        }
        break;

      default:
        return n;
    }

    Node newNode = new Node(result ? Token.TRUE : Token.FALSE);
    n.getParent().replaceChild(n, newNode);
    reportCodeChange();

    return newNode;
  }