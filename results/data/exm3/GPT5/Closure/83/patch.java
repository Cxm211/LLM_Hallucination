public int parseArguments(Parameters params) throws CmdLineException {
        String param = params.getParameter(0);

        if (param == null) {
          setter.addValue(true);
          return 0;
        } else {
          String lowerParam = param.toLowerCase(java.util.Locale.ENGLISH);
          if (TRUES.contains(lowerParam)) {
            setter.addValue(true);
            return 1;
          } else if (FALSES.contains(lowerParam)) {
            setter.addValue(false);
            return 1;
          } else if (param.startsWith("-")) {
            // Next token is another option; treat this flag as a switch without a value.
            setter.addValue(true);
            return 0;
          } else {
            throw new CmdLineException(owner, "Invalid boolean value: " + param);
          }
        }
      }