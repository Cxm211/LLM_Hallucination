public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
     
        PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
        if (plot != null) {
            state.setTotal(DatasetUtilities.calculatePieDatasetTotal(
                    plot.getDataset()));
            state.setLatestAngle(plot.getStartAngle());
        } else {
            state.setTotal(0.0);
            state.setLatestAngle(0.0);
        }
        return state;
        
    }