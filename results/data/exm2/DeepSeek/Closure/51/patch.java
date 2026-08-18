  void addNumber(double x) {
    // This is not pretty printing. This is to prevent misparsing of x- -4 as
    // x--4 (which is a syntax error).
    boolean isNegativeZero = (x == 0.0 && 1/x < 0);
    boolean isNegative = x < 0 || isNegativeZero;
    char prev = getLastChar();
    if (isNegative && prev == '-') {
      add(" ");
    }

    if ((long) x == x && !isNegativeZero) {
      long value = (long) x;
      long mantissa = value;
      int exp = 0;
      if (Math.abs(x) >= 100) {
        while (mantissa / 10 * Math.pow(10, exp + 1) == value) {
          mantissa /= 10;
          exp++;
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