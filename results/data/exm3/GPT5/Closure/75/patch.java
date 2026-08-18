static Double getStringNumberValue(String rawJsString) {
      // vertical tab is not always whitespace; if present at the string boundaries,
      // different JS engines disagree on trimming behavior. Be conservative.
      if (rawJsString != null && rawJsString.length() > 0) {
        char first = rawJsString.charAt(0);
        char last = rawJsString.charAt(rawJsString.length() - 1);
        if (first == '\u000B' || last == '\u000B') {
          return null;
        }
      }

    String s = trimJsWhiteSpace(rawJsString);
    if (s.length() == 0) {
      return 0.0;
    }

    if (s.length() > 2
        && s.charAt(0) == '0'
        && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
      // Attempt to convert hex numbers.
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
      // hex numbers with explicit signs vary between browsers.
      return null;
    }

    // FireFox and IE treat the "Infinity" differently. FireFox is case
    // insensitive, but IE treats "infinity" as NaN.  So leave it alone.
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