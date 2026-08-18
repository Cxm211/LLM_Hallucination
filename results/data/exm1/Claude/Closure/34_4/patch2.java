void appendOp(String op, boolean binOp) {
      if (binOp) {
        append(" ");
        append(op);
        append(" ");
      } else {
        append(op);
      }
    }