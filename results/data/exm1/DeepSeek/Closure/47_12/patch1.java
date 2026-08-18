  public void addMapping(
      Node node,
      FilePosition outputStartPosition,
      FilePosition outputEndPosition) {
    String sourceFile = node.getSourceFileName();
    if (sourceFile == null || node.getLineno() < 0) {
      return;
    }
    sourceFile = fixupSourceLocation(sourceFile);
    String originalName = (String) node.getProp(Node.ORIGINALNAME_PROP);
    generator.addMapping(
        sourceFile, originalName,
        new FilePosition(node.getLineno(), node.getCharno()),
        outputStartPosition, outputEndPosition);
  }