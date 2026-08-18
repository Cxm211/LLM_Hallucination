void addNumber(double x) {
    char prev = getLastChar();
    boolean negativeZero = isNegativeZero(x);
    if (x < 0 && prev == '-') {
      add(" ");
    }

    if ((long) x == x && !negativeZero) {
      long value = (long) x;
      long mantissa = value;
      int exp = 0;
      if (Math.abs(x) >= 100) {
        while (value % 10 == 0) {
          value /= 10;
          exp++;
        }
        mantissa = value;
      }
      if (exp > 2) {
        add(Long.toString(mantissa) + "E" + Integer.toString(exp));
      } else {
        add(Long.toString((long) x));
      }
    } else {
      add(String.valueOf(x));
    }
  }