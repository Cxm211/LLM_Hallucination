void appendOp(String op, boolean binOp) {
      if (binOp) {
        if (getLastChar() != ' ') {
          append(" ");
        }
        append(op);
        append(" ");
      } else {
        append(op);
        if (op.matches("[a-zA-Z]+")) {
          append(" ");
        }
      }
    }