// ===== FIXED com.google.javascript.jscomp.PeepholeFoldConstants :: tryFoldTypeof(Node) [lines 156-203] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-148-fixed/src/com/google/javascript/jscomp/PeepholeFoldConstants.java =====
  private Node tryFoldTypeof(Node originalTypeofNode) {
    Preconditions.checkArgument(originalTypeofNode.getType() == Token.TYPEOF);

    Node argumentNode = originalTypeofNode.getFirstChild();
    if (argumentNode == null || !NodeUtil.isLiteralValue(argumentNode)) {
      return originalTypeofNode;
    }

    String typeNameString = null;

    switch (argumentNode.getType()) {
      case Token.STRING:
        typeNameString = "string";
        break;
      case Token.NUMBER:
        typeNameString = "number";
        break;
      case Token.TRUE:
      case Token.FALSE:
        typeNameString = "boolean";
        break;
      case Token.NULL:
      case Token.OBJECTLIT:
      case Token.ARRAYLIT:
        typeNameString = "object";
        break;
      case Token.VOID:
        typeNameString = "undefined";
        break;
      case Token.NAME:
        // We assume here that programs don't change the value of the
        // keyword undefined to something other than the value undefined.
        if ("undefined".equals(argumentNode.getString())) {
          typeNameString = "undefined";
        }
        break;
    }

    if (typeNameString != null) {
      Node newNode = Node.newString(typeNameString);
      originalTypeofNode.getParent().replaceChild(originalTypeofNode, newNode);
      reportCodeChange();

      return newNode;
    }

    return originalTypeofNode;
  }

// ===== FIXED com.google.javascript.jscomp.SourceMap :: addMapping(Node, Position, Position) [lines 181-232] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-148-fixed/src/com/google/javascript/jscomp/SourceMap.java =====
  void addMapping(Node node, Position startPosition, Position endPosition) {
    String sourceFile = (String)node.getProp(Node.SOURCEFILE_PROP);

    // If the node does not have an associated source file or
    // its line number is -1, then the node does not have sufficient
    // information for a mapping to be useful.
    if (sourceFile == null || node.getLineno() < 0) {
      return;
    }

    // Create the new mapping.
    Mapping mapping = new Mapping();
    mapping.sourceFile = sourceFile;
    mapping.originalPosition = new Position(node.getLineno(), node.getCharno());

    String originalName = (String)node.getProp(Node.ORIGINALNAME_PROP);
    if (originalName != null) {
      mapping.originalName = originalName;
    }

    if (offsetPosition.getLineNumber() == 0
        && offsetPosition.getCharacterIndex() == 0) {
      mapping.startPosition = startPosition;
      mapping.endPosition = endPosition;
    } else {
      // If the mapping is found on the first line, we need to offset
      // its character position by the number of characters found on
      // the *last* line of the source file to which the code is
      // being generated.
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
    }

    mappings.add(mapping);
  }

// ===== FIXED com.google.javascript.jscomp.SourceMap :: appendTo(Appendable, String) [lines 291-346] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-148-fixed/src/com/google/javascript/jscomp/SourceMap.java =====
  public void appendTo(Appendable out, String name) throws IOException {
    // Write the mappings out to the file. The format of the generated
    // source map is three sections, each deliminated by a magic comment.
    //
    // The first section contains an array for each line of the generated
    // code, where each element in the array is the ID of the mapping which
    // best represents the index-th character found on that line of the
    // generated source code.
    //
    // The second section contains an array per generated line. Unused.
    //
    // The third and final section contains an array per line, each of which
    // represents a mapping with a unique ID. The mappings are added in order.
    // The array itself contains a tuple representing
    // ['source file', line, col (, 'original name')]
    //
    // Example for 2 lines of generated code (with line numbers added for
    // readability):
    //
    // 1)  /** Begin line maps. **/{ "count": 2 }
    // 2)  [0,0,0,0,0,0,1,1,1,1,2]
    // 3)  [2,2,2,2,2,2,3,4,4,4,4,4]
    // 4)  /** Begin file information. **/
    // 5)  []
    // 6)  []
    // 7)  /** Begin mapping definitions. **/
    // 8)  ["a.js", 1, 34]
    // 9)  ["a.js", 5, 2]
    // 10) ["b.js", 1, 3, "event"]
    // 11) ["c.js", 1, 4]
    // 12) ["d.js", 3, 78, "foo"]

    int maxLine = prepMappings();

    // Add the line character maps.
    out.append("/** Begin line maps. **/{ \"file\" : ");
    out.append(escapeString(name));
    out.append(", \"count\": ");
    out.append(String.valueOf(maxLine + 1));
    out.append(" }\n");
    (new LineMapper(out)).appendLineMappings();

    // Add the source file maps.
    out.append("/** Begin file information. **/\n");

    // This section is unused but we need one entry per line to
    // prevent changing the format.
    for (int i = 0; i <= maxLine; ++i) {
      out.append("[]\n");
    }

    // Add the mappings themselves.
    out.append("/** Begin mapping definitions. **/\n");

    (new MappingWriter()).appendMappings(out);
  }
