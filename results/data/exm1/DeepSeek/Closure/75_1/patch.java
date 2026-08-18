  static Double getStringNumberValue(String rawJsString) {
      String s = trimJsWhiteSpace(rawJsString);
      if (s.length() == 0) {
        return 0.0;
      }

      if (s.length() > 2
          && s.charAt(0) == '0'
          && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
        try {
          return Double.valueOf(Integer.parseInt(s.substring(2), 16));
        } catch (NumberFormatException e) {
          return Double.NaN;
        }
      }

      if (s.length() > 3
          && (s.charAt(0) == '-' || s.charAt(0) == '+')
          && s.charAt(1) == '0'
          && (s.charAt(2) == 'x' || s.charAt(2) == 'X')) {
        try {
          String hexPart = s.substring(3);
          int sign = (s.charAt(0) == '-') ? -1 : 1;
          return Double.valueOf(sign * Integer.parseInt(hexPart, 16));
        } catch (NumberFormatException e) {
          return Double.NaN;
        }
      }

      if (s.equalsIgnoreCase("infinity")) {
        return Double.POSITIVE_INFINITY;
      }
      if (s.equalsIgnoreCase("-infinity")) {
        return Double.NEGATIVE_INFINITY;
      }
      if (s.equalsIgnoreCase("+infinity")) {
        return Double.POSITIVE_INFINITY;
      }

      try {
        return Double.parseDouble(s);
      } catch (NumberFormatException e) {
        return Double.NaN;
      }
    }