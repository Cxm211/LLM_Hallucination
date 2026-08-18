void addMapping(Node node, Position startPosition, Position endPosition) {
    String sourceFile = (String)node.getProp(Node.SOURCEFILE_PROP);

    if (sourceFile == null || node.getLineno() < 0) {
      return;
    }

    String escapedSourceFile;
    if (lastSourceFile != sourceFile) {
      lastSourceFile = sourceFile;
      lastSourceFileEscaped = escapeString(sourceFile);
    }
    escapedSourceFile = lastSourceFileEscaped;

    Mapping mapping = new Mapping();
    mapping.id = mappings.size();
    mapping.sourceFile = escapedSourceFile;
    mapping.originalPosition = new Position(node.getLineno(), node.getCharno());

    String originalName = (String)node.getProp(Node.ORIGINALNAME_PROP);
    if (originalName != null) {
      mapping.originalName = escapeString(originalName);
    }

    int offsetLine = offsetPosition.getLineNumber();
    int startOffsetPosition = offsetPosition.getCharacterIndex();
    int endOffsetPosition = offsetPosition.getCharacterIndex();

    if (startPosition.getLineNumber() > 0) {
      startOffsetPosition = 0;
    }

    if (endPosition.getLineNumber() > 0) {
      endOffsetPosition = 0;
    }

    mapping.startPosition =
        new Position(startPosition.getLineNumber() + offsetLine,
                     startPosition.getCharacterIndex() + startOffsetPosition);

    mapping.endPosition =
        new Position(endPosition.getLineNumber() + offsetLine,
                     endPosition.getCharacterIndex() + endOffsetPosition);

    mappings.add(mapping);
  }