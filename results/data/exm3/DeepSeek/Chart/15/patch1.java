    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot plot, Integer index, PlotRenderingInfo info) {
     
        PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
        PieDataset dataset = plot.getDataset();
        double total;
        if (dataset == null) {
            total = 0.0;
        } else {
            total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        }
        state.setTotal(total);
        state.setLatestAngle(plot.getStartAngle());
        return state;
        
    }