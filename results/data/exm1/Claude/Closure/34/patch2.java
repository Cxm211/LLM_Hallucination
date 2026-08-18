void appendOp(String op, boolean binOp) {
      if (binOp) {
        if (getLastChar() != ' ') {
          append(" ");
        }
        append(op);
        if (op.charAt(op.length() - 1) != ' ') {
          append(" ");
        }
      } else {
        append(op);
      }
    }