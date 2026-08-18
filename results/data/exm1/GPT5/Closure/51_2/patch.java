void addNumber(double x) {
    char prev = getLastChar();
    if (x < 0 && prev == '-') {
      add(" ");
    }

    if ((long) x == x) {
      long value = (long) x;
      long mantissa = value;
      int exp = 0;
      if (Math.abs(value) >= 100) {
        while (mantissa % 10 == 0) {
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