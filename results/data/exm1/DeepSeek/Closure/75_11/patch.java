static Double getStringNumberValue(String rawJsString) {
      if (rawJsString.indexOf('\u000B') != -1) {
        return null;
      }
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
        return null;
      }
      if (s.equals("infinity")
          || s.equals("-infinity")
          || s.equals("+infinity")) {
        return null;
      }
      try {
        return Double.parseDouble(s);
      } catch (NumberFormatException e) {
        return Double.NaN;
      }
    }