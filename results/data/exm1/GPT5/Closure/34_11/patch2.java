    void appendOp(String op, boolean binOp) {
      if (binOp) {
        // For binary operators, avoid forcing spaces around the operator.
        // The surrounding spacing will be handled only when necessary
        // by other parts of the code generator to minimize output.
        append(op);
      } else {
        append(op);
      }
    }
