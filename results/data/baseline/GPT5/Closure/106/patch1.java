public boolean recordBlockDescription(String description) {
    if (!parseDocumentation) {
      return true;
    }
    populated = true;
    return currentInfo.documentBlock(description);
  }