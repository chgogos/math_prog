package mathmodeler;

public class Objective {
	LPExpression expr;
	String sense;

	public Objective(String sense) {
		this.sense = sense;
	}

	public void setExpr(LPExpression expr) {
		this.expr = expr;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s\n\t%s", sense, expr.toString()));
		return sb.toString();
	}

	public String getSense() {
		return sense;
	}

	public LPExpression getExpr() {
		return expr;
	}
}
