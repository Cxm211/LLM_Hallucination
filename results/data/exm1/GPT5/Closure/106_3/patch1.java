public boolean recordBlockDescription(String description) {
    if (!parseDocumentation) {
      return false;
    }
    populated = true;
    return currentInfo.documentBlock(description);
  }