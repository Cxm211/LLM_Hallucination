public boolean recordBlockDescription(String description) {
    if (!parseDocumentation) {
      return true;
    }
    boolean result = currentInfo.documentBlock(description);
    if (result) {
      populated = true;
    }
    return result;
  }