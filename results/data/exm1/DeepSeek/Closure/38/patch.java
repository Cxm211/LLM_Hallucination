void addNumber(double x) {
    char prev = getLastChar();
    boolean negativeZero = isNegativeZero(x);
    if (x < 0 && prev == '-') {
      add(" ");
    }

    if ((long) x == x && !negativeZero) {
      long value = (long) x;
      if (Math.abs(x) >= 100) {
        long mantissa = value;
        int exp = 0;
        while (mantissa % 10 == 0) {
          mantissa /= 10;
          exp++;
        }
        if (exp > 2) {
          add(Long.toString(mantissa) + "E" + Integer.toString(exp));
          return;
        }
      }
      add(Long.toString(value));
    } else {
      add(String.valueOf(x));
    }
  }