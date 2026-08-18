// ===== FIXED org.jfree.chart.plot.PiePlot :: initialise(Graphics2D, Rectangle2D, PiePlot, Integer, PlotRenderingInfo) [lines 2049-2061] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-15-fixed/source/org/jfree/chart/plot/PiePlot.java =====
    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
     
        PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
        if (this.dataset != null) {
            state.setTotal(DatasetUtilities.calculatePieDatasetTotal(
                    plot.getDataset()));
        }
        state.setLatestAngle(plot.getStartAngle());
        return state;
        
    }
