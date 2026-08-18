    void appendOp(String op, boolean binOp) {
      if (binOp) {
        // Only add spaces around word operators to prevent token merging.
        if ("in".equals(op) || "instanceof".equals(op)) {
          if (getLastChar() != ' ') {
            append(" ");
          }
          append(op);
          append(" ");
        } else {
          append(op);
        }
      } else {
        append(op);
      }
    }