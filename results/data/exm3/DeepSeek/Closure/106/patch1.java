  public boolean recordBlockDescription(String description) {
    populated = parseDocumentation;
    return currentInfo.documentBlock(description);
  }