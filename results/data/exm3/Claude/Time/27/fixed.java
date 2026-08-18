// ===== FIXED org.joda.time.format.PeriodFormatterBuilder :: toFormatter(List, boolean, boolean) [lines 794-815] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-27-fixed/src/main/java/org/joda/time/format/PeriodFormatterBuilder.java =====
    private static PeriodFormatter toFormatter(List<Object> elementPairs, boolean notPrinter, boolean notParser) {
        if (notPrinter && notParser) {
            throw new IllegalStateException("Builder has created neither a printer nor a parser");
        }
        int size = elementPairs.size();
        if (size >= 2 && elementPairs.get(0) instanceof Separator) {
            Separator sep = (Separator) elementPairs.get(0);
            if (sep.iAfterParser == null && sep.iAfterPrinter == null) {
                PeriodFormatter f = toFormatter(elementPairs.subList(2, size), notPrinter, notParser);
                sep = sep.finish(f.getPrinter(), f.getParser());
                return new PeriodFormatter(sep, sep);
            }
        }
        Object[] comp = createComposite(elementPairs);
        if (notPrinter) {
            return new PeriodFormatter(null, (PeriodParser) comp[1]);
        } else if (notParser) {
            return new PeriodFormatter((PeriodPrinter) comp[0], null);
        } else {
            return new PeriodFormatter((PeriodPrinter) comp[0], (PeriodParser) comp[1]);
        }
    }
