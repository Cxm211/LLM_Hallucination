public static boolean isJSIdentifier(String s) {
      if (s == null) {
        return false;
      }
      int length = s.length();
      if (length == 0) {
        return false;
      }

      int lastDot = -1;
      boolean atSegmentStart = true;
      for (int i = 0; i < length; i++) {
        char c = s.charAt(i);
        if (c == '.') {
          // segment cannot be empty
          if (atSegmentStart) {
            return false;
          }
          // check keyword for the segment we just parsed
          String seg = s.substring(lastDot + 1, i);
          if ("class".equals(seg)) {
            return false;
          }
          atSegmentStart = true;
          lastDot = i;
          continue;
        }
        if (atSegmentStart) {
          if (!Character.isJavaIdentifierStart(c)) {
            return false;
          }
          atSegmentStart = false;
        } else {
          if (!Character.isJavaIdentifierPart(c)) {
            return false;
          }
        }
      }
      // cannot end with a dot
      if (atSegmentStart) {
        return false;
      }
      // check last segment keyword
      String lastSeg = s.substring(lastDot + 1);
      if ("class".equals(lastSeg)) {
        return false;
      }
      return true;
    }