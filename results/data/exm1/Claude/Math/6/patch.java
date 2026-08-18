protected BaseOptimizer(ConvergenceChecker<PAIR> checker) {
    this.checker = checker;
    evaluations = new Incrementor(Integer.MAX_VALUE, new MaxEvalCallback());
    iterations = new Incrementor(Integer.MAX_VALUE, new MaxIterCallback());
}