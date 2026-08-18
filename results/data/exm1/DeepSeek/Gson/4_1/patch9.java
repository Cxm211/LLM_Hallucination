private void beforeValue(boolean root) throws IOException {
    switch (peek()) {
    case NONEMPTY_DOCUMENT:
      if (!lenient) {
        throw new IllegalStateException(
            "JSON must have only one top-level value.");
      }
    case EMPTY_DOCUMENT:
      if (!lenient && !root) {
        throw new IllegalStateException(
            "JSON must start with an array or an object.");
      }
      replaceTop(NONEMPTY_DOCUMENT);
      break;

    case EMPTY_ARRAY:
      replaceTop(NONEMPTY_ARRAY);
      newline();
      break;

    case NONEMPTY_ARRAY:
      out.append(',');
      newline();
      break;

    case DANGLING_NAME:
      out.append(separator);
      replaceTop(NONEMPTY_OBJECT);
      break;

    default:
      throw new IllegalStateException("Nesting problem.");
    }
  }