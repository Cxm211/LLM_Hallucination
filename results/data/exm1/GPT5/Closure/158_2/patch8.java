  private void initConfigFromFlags(String[] args, PrintStream err) {

    List<String> processedArgs = processArgs(args);

    CmdLineParser parser = new CmdLineParser(flags);
    isConfigValid = true;
    try {
      parser.parseArgument(processedArgs.toArray(new String[] {}));
      // For contains --flagfile flag
      if (!flags.flag_file.equals("")) {
        processFlagFile(err);
      }
    } catch (CmdLineException e) {
      err.println(e.getMessage());
      isConfigValid = false;
    } catch (IOException ioErr) {
      err.println("ERROR - " + flags.flag_file + " read error.");
      isConfigValid = false;
    }

    if (flags.version) {
      ResourceBundle config = ResourceBundle.getBundle(configResource);
      err.println(
          "Closure Compiler (http://code.google.com/closure/compiler)\n" +
          "Version: " + config.getString("compiler.version") + "\n" +
          "Built on: " + config.getString("compiler.date"));
      err.flush();
    }

    if (!isConfigValid || flags.display_help) {
      isConfigValid = false;
      parser.printUsage(err);
    } else {
      // Compute last-occurrence semantics for warning guards based on arg order
      java.util.Map<String, CheckLevel> lastLevels = new java.util.LinkedHashMap<String, CheckLevel>();
      for (String a : processedArgs) {
        if (a.startsWith("--jscomp_error=")) {
          String v = a.substring(a.indexOf('=') + 1);
          for (String n : v.split(",")) {
            if (!n.isEmpty()) lastLevels.put(n, CheckLevel.ERROR);
          }
        } else if (a.startsWith("--jscomp_warning=")) {
          String v = a.substring(a.indexOf('=') + 1);
          for (String n : v.split(",")) {
            if (!n.isEmpty()) lastLevels.put(n, CheckLevel.WARNING);
          }
        } else if (a.startsWith("--jscomp_off=")) {
          String v = a.substring(a.indexOf('=') + 1);
          for (String n : v.split(",")) {
            if (!n.isEmpty()) lastLevels.put(n, CheckLevel.OFF);
          }
        }
      }
      java.util.List<String> finalErrors = Lists.newArrayList();
      java.util.List<String> finalWarnings = Lists.newArrayList();
      java.util.List<String> finalOff = Lists.newArrayList();
      if (!lastLevels.isEmpty()) {
        for (java.util.Map.Entry<String, CheckLevel> e : lastLevels.entrySet()) {
          if (e.getValue() == CheckLevel.ERROR) {
            finalErrors.add(e.getKey());
          } else if (e.getValue() == CheckLevel.WARNING) {
            finalWarnings.add(e.getKey());
          } else if (e.getValue() == CheckLevel.OFF) {
            finalOff.add(e.getKey());
          }
        }
      } else {
        finalErrors.addAll(flags.jscomp_error);
        finalWarnings.addAll(flags.jscomp_warning);
        finalOff.addAll(flags.jscomp_off);
      }

      getCommandLineConfig()
          .setPrintTree(flags.print_tree)
          .setComputePhaseOrdering(flags.compute_phase_ordering)
          .setPrintAst(flags.print_ast)
          .setPrintPassGraph(flags.print_pass_graph)
          .setJscompDevMode(flags.jscomp_dev_mode)
          .setLoggingLevel(flags.logging_level)
          .setExterns(flags.externs)
          .setJs(flags.js)
          .setJsOutputFile(flags.js_output_file)
          .setModule(flags.module)
          .setVariableMapInputFile(flags.variable_map_input_file)
          .setPropertyMapInputFile(flags.property_map_input_file)
          .setVariableMapOutputFile(flags.variable_map_output_file)
          .setCreateNameMapFiles(flags.create_name_map_files)
          .setPropertyMapOutputFile(flags.property_map_output_file)
          .setCodingConvention(flags.third_party ?
               new DefaultCodingConvention() :
               new ClosureCodingConvention())
          .setSummaryDetailLevel(flags.summary_detail_level)
          .setOutputWrapper(flags.output_wrapper)
          .setModuleWrapper(flags.module_wrapper)
          .setModuleOutputPathPrefix(flags.module_output_path_prefix)
          .setCreateSourceMap(flags.create_source_map)
          .setJscompError(finalErrors)
          .setJscompWarning(finalWarnings)
          .setJscompOff(finalOff)
          .setDefine(flags.define)
          .setCharset(flags.charset)
          .setManageClosureDependencies(flags.manage_closure_dependencies)
          .setClosureEntryPoints(flags.closure_entry_point)
          .setOutputManifest(flags.output_manifest)
          .setAcceptConstKeyword(flags.accept_const_keyword)
          .setLanguageIn(flags.language_in);
    }
  }