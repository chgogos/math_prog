package mathmodeler;

import java.text.NumberFormat;
import java.util.Locale;

public class LPConstraint {
	String constraintName;
	LPExpression expr;
	double rhs;
	double lhs;

	public LPConstraint(String constraintName) {
		this.constraintName = constraintName;
	}

	@Override
	public String toString() {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setGroupingUsed(false);
		if (lhs == -Double.MAX_VALUE)
			return String.format("%s: %s <= %s", constraintName,
					expr.toString(), nf.format(rhs));
		else if (rhs == Double.MAX_VALUE)
			return String.format("%s: %s >= %s ", constraintName,
					expr.toString(), nf.format(lhs));
		else if (lhs == rhs)
			return String.format("%s: %s = %s", constraintName,
					expr.toString(), nf.format(rhs));
		else
			return String.format("%s: %s <= %s <= %s", constraintName,
					nf.format(rhs), expr.toString(), nf.format(rhs));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constraintName == null) ? 0 : constraintName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(lhs);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(rhs);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LPConstraint other = (LPConstraint) obj;
		if (constraintName == null) {
			if (other.constraintName != null)
				return false;
		} else if (!constraintName.equals(other.constraintName))
			return false;
		if (Double.doubleToLongBits(lhs) != Double.doubleToLongBits(other.lhs))
			return false;
		if (Double.doubleToLongBits(rhs) != Double.doubleToLongBits(other.rhs))
			return false;
		return true;
	}

	public double getLhs() {
		return lhs;
	}

	public double getRhs() {
		return rhs;
	}

	public void setRhs(double rhs) {
		this.rhs = rhs;
	}

	public void setLhs(double lhs) {
		this.lhs = lhs;
	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String name) {
		this.constraintName = name;
	}

	public void setExpression(LPExpression expr) {
		this.expr = expr;
	}

	public LPExpression getExpr() {
		return expr;
	}

}
