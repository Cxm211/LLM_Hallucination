import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.Compiler;

public abstract class CoreOperationRelationalExpression extends CoreOperation {
    protected CoreOperationRelationalExpression(Expression[] args) {
        super(args);
    }
    public abstract Object computeValue(EvalContext context);
    protected final boolean isSymmetric() {
        return false;
    }
}