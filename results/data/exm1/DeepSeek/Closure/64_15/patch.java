public void toSource(final CodeBuilder cb,
                       final int inputSeqNum,
                       final Node root) {
    initCompilerOptionsIfTesting();
    runInCompilerThread(new Callable<Void>() {
      public Void call() throws Exception {
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

        if (options.sourceMapOutputPath != null) {
          sourceMap.setStartingPosition(
              cb.getLineIndex(), cb.getColumnIndex());
        }

        String code = toSource(root, sourceMap);
        if (!code.isEmpty()) {
          cb.append(code);

          String trimmedCode = code.trim();
          boolean hasSemiColon = trimmedCode.endsWith(";");
          if (!hasSemiColon) {
            cb.append(";");
          }
        }
        return null;
      }
    });
  }