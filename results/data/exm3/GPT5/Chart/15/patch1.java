public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
        PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
        double total = 0.0;
        if (plot != null && plot.getDataset() != null) {
            total = DatasetUtilities.calculatePieDatasetTotal(plot.getDataset());
        }
        state.setTotal(total);
        state.setLatestAngle(plot != null ? plot.getStartAngle() : 0.0);
        return state;
    }