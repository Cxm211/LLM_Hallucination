  void addNumber(double x) {
    // This is not pretty printing. This is to prevent misparsing of x- -4 as
    // x--4 (which is a syntax error).
    char prev = getLastChar();
    if (x < 0 && prev == '-') {
      add(" ");
    }

    if ((long) x == x) {
      long value = (long) x;
      if (Math.abs(x) >= 100) {
        int exp = 0;
        while (value % 10 == 0) {
          value /= 10;
          exp++;
        }
        if (exp > 2) {
          add(Long.toString(value) + "E" + Integer.toString(exp));
        } else {
          add(Long.toString((long) x));
        }
      } else {
        add(Long.toString(value));
      }
    } else {
      add(String.valueOf(x));
    }
  }
