public void toSource(final CodeBuilder cb,
                       final int inputSeqNum,
                       final Node root) {
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

          
          int length = code.length();
          char lastChar = code.charAt(length - 1);
          
          boolean hasSemiColon = false;
          if (lastChar == ';') {
            hasSemiColon = true;
          } else if (lastChar == '\n') {
            
            for (int i = length - 2; i >= 0; i--) {
              char c = code.charAt(i);
              if (c == ';') {
                hasSemiColon = true;
                break;
              } else if (c != '\n' && c != '\r') {
                break;
              }
            }
          }
          if (!hasSemiColon) {
            cb.append(";");
          }
        }
        return null;
      }
    });
  }