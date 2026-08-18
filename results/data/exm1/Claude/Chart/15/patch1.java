public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
        PiePlot plot, Integer index, PlotRenderingInfo info) {
 
    PiePlotState state = new PiePlotState(info);
    state.setPassesRequired(2);
    state.setTotal(DatasetUtilities.calculatePieDatasetTotal(
            this.dataset));
    state.setLatestAngle(this.startAngle);
    return state;
    
}