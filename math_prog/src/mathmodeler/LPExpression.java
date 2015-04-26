package mathmodeler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LPExpression {
	List<DecisionVariable> variables;
	List<Double> coefs;

	public LPExpression() {
		variables = new ArrayList<DecisionVariable>();
		coefs = new ArrayList<Double>();
	}

	public void addTerm(double coef, DecisionVariable dv) {
		coefs.add(coef);
		variables.add(dv);
	}

	@Override
	public String toString() {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setGroupingUsed(false);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < variables.size(); i++) {
			DecisionVariable dv = variables.get(i);
			double coef = coefs.get(i);
			if (coef == 1.0)
				sb.append(String.format("%s", dv.name));
			else
				sb.append(String.format("%s %s", nf.format(coef), dv.name));
			if (i < variables.size() - 1)
				sb.append(" + ");
		}
		return sb.toString().replace("+ -", "-");
	}

	public List<DecisionVariable> getVariables() {
		return variables;
	}

	public List<Double> getCoefs() {
		return coefs;
	}

}
