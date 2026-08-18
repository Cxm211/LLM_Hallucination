  void addNumber(double x) {
    // This is not pretty printing. This is to prevent misparsing of x- -4 as
    // x--4 (which is a syntax error).
    char prev = getLastChar();
    if (x < 0 && prev == '-') {
      add(" ");
    }

    if ((long) x == x) {
      long value = (long) x;
      long mantissa = value;
      int exp = 0;
      long absVal = Math.abs(value);
      if (absVal >= 100) {
        while (absVal != 0 && absVal % 10 == 0) {
          absVal /= 10;
          exp++;
        }
        // Reduce mantissa by the number of trailing zeros found.
        for (int i = 0; i < exp; i++) {
          mantissa /= 10;
        }
      }
      if (exp > 2) {
        add(Long.toString(mantissa) + "E" + Integer.toString(exp));
      } else {
        add(Long.toString(value));
      }
    } else {
      add(String.valueOf(x));
    }

  }