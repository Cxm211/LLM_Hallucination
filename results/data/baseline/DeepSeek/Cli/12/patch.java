protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption)
{
    List tokens = new ArrayList();

    boolean eatTheRest = false;

    for (int i = 0; i < arguments.length; i++)
    {
        String arg = arguments[i];

        if ("--".equals(arg))
        {
            eatTheRest = true;
            tokens.add("--");
        }
        else if ("-".equals(arg))
        {
            tokens.add("-");
        }
        else if (arg.startsWith("-"))
        {
            // New handling for options with attached value using '='
            String stripped = Util.stripLeadingHyphens(arg);
            int eqIdx = stripped.indexOf('=');
            String opt = stripped;
            String value = null;
            if (eqIdx != -1) {
                opt = stripped.substring(0, eqIdx);
                value = stripped.substring(eqIdx + 1);
            }

            if (options.hasOption(opt)) {
                // Option is known; add the option token and value if present
                String prefix = arg.startsWith("--") ? "--" : "-";
                tokens.add(prefix + opt);
                if (value != null) {
                    tokens.add(value);
                }
            } else if (options.hasOption(arg.substring(0, 2))) {
                // the format is --foo=value or -foo=value
                // the format is a special properties option (-Dproperty=value)
                tokens.add(arg.substring(0, 2)); // -D
                tokens.add(arg.substring(2)); // property=value
            } else {
                if (stopAtNonOption) {
                    eatTheRest = true;
                }
                tokens.add(arg);
            }
        }
        else
        {
            tokens.add(arg);
        }

        if (eatTheRest)
        {
            for (i++; i < arguments.length; i++)
            {
                tokens.add(arguments[i]);
            }
        }
    }

    return (String[]) tokens.toArray(new String[tokens.size()]);
}