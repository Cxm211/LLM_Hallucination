public void toSource(final CodeBuilder cb,
                       final int inputSeqNum,
                       final Node root) {
    runInCompilerThread(new Callable<Void>() {
      public Void call() throws Exception {
        // Set starting position before any output for this file
        if (options.sourceMapOutputPath != null) {
          sourceMap.setStartingPosition(
              cb.getLineIndex(), cb.getColumnIndex());
        }

        if (options.printInputDelimiter) {
          if ((cb.getLength() > 0) && !cb.endsWith("\n")) {
            cb.append("\n");
          }
          Preconditions.checkState(root.getType() == Token.SCRIPT);

          String delimiter = options.inputDelimiter;

          String sourceName = (String)root.getProp(Node.SOURCENAME_PROP);
          Preconditions.checkState(sourceName != null);
          Preconditions.checkState(!sourceName.isEmpty());

          delimiter = delimiter.replaceAll("%name%", sourceName)
            .replaceAll("%num%", String.valueOf(inputSeqNum));

          cb.append(delimiter)
            .append("\n");
        }
        if (root.getJSDocInfo() != null &&
            root.getJSDocInfo().getLicense() != null) {
          cb.append("/*\n")
            .append(root.getJSDocInfo().getLicense())
            .append("*/\n");
        }

        String code = toSource(root, sourceMap);
        if (!code.isEmpty()) {
          cb.append(code);

          int length = code.length();
          char lastChar = code.charAt(length - 1);
          char secondLastChar = length >= 2 ?
              code.charAt(length - 2) : '\0';
          boolean hasSemiColon = lastChar == ';' ||
              (lastChar == '\n' && secondLastChar == ';');
          if (!hasSemiColon) {
            cb.append(";");
          }
        }
        return null;
      }
    });
  }