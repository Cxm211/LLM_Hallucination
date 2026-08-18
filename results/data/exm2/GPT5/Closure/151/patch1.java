    private void applyToOptions(CompilerOptions options) {
      switch (this) {
        case PRETTY_PRINT:
          options.prettyPrint = true;
          break;
        case PRINT_INPUT_DELIMITER:
          options.printInputDelimiter = true;
          break;
        default:
          throw new RuntimeException("Unknown formatting option: " + this);
      }
    }