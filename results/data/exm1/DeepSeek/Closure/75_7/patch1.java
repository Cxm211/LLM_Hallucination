  static TernaryValue isStrWhiteSpaceChar(int c) {
    switch (c) {
      case ' ': // <SP>
      case '
': // <LF>
      case '': // <CR>
      case '	': // <TAB>
      case ' ': // <NBSP>
      case '': // <FF>
      case ' ': // <LS>
      case ' ': // <PS>
      case '﻿': // <BOM>
        return TernaryValue.TRUE;
      default:
        return (Character.getType(c) == Character.SPACE_SEPARATOR)
            ? TernaryValue.TRUE : TernaryValue.FALSE;
    }
  }