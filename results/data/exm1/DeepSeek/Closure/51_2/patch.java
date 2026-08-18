  void addNumber(double x) {
    char prev = getLastChar();
    if (x < 0 && prev == '-') {
      add(" ");
    }

    if ((long) x == x) {
      long value = (long) x;
      long mantissa = value;
      int exp = 0;
      if (Math.abs(x) >= 100 && mantissa != 0) {
        long temp = mantissa;
        while (temp % 10 == 0) {
          temp /= 10;
          exp++;
        }
        mantissa = temp;
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