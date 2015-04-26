package ui;
import gurobi.GRBException;
import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mathmodeler.DecisionVariable;
import mathmodeler.LPConstraint;
import mathmodeler.LPExpression;
import mathmodeler.MathAgentGurobiCplex;
import mathmodeler.Objective;

import org.apache.commons.lang3.StringUtils;

public class TestMathAgent {

	public static void main(String[] args) {
		TestMathAgent app = new TestMathAgent();
		app.testMathAgent();
	}

	void testMathAgent() {
		System.out.println("Linear Programming");
		System.out.println(StringUtils.repeat('#', 40));
		testGurobi();
		System.out.println(StringUtils.repeat('#', 40));
		testCplex();
	}

	void testGurobi() {
		MathAgentGurobiCplex mathAgentLP = getMathAgentForLP();
		System.out.println("Gurobi LP");
		System.out.println(StringUtils.repeat('*', 40));
		solveUsingGurobiLP(mathAgentLP);
		System.out.println(StringUtils.repeat('*', 40));
		System.out.println("Gurobi IP");
		MathAgentGurobiCplex mathAgentIP = getMathAgentForIP();
		solveUsingGurobiIP(mathAgentIP);
	}

	void testCplex() {
		MathAgentGurobiCplex mathAgentLP = getMathAgentForLP();
		System.out.println("Cplex LP");
		System.out.println(StringUtils.repeat('*', 40));
		solveUsingCplexLP(mathAgentLP);
		System.out.println(StringUtils.repeat('*', 40));
		System.out.println("Cplex IP");
		MathAgentGurobiCplex mathAgentIP = getMathAgentForIP();
		solveUsingCplexIP(mathAgentIP);
	}

	private void solveUsingGurobiLP(MathAgentGurobiCplex mathAgent) {
		try {
			mathAgent.prepareGurobi();
			mathAgent.solveUsingGurobi();
			mathAgent.computeDualsUsingGurobi();
			List<String> dv_names = Arrays.asList("x0", "x1", "x2", "x3");
			double objvalue = mathAgent.getObjectiveValue();
			System.out.printf("Objective value=%.2f\n", objvalue);
			for (String dv : dv_names) {
				DecisionVariable decision_variable = mathAgent
						.getSingleDecisionVariable(dv);
				System.out
						.printf("%s=%.2f basis=%s rc=%.2f\n",
								dv,
								decision_variable.getValue(),
								mathAgent
										.getBasisStatusForVariableUsingGurobi(decision_variable),
								decision_variable.getReducedCost());
			}
			for (int i = 0; i < mathAgent.getConstraints().size(); i++)
				System.out.printf("Dual value of constraint %d = %.2f\n", i,
						mathAgent.getDuals()[i]);
			mathAgent.disposeUsingGurobi();
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}

	private void solveUsingGurobiIP(MathAgentGurobiCplex mathAgent) {
		try {
			mathAgent.prepareGurobi();
			mathAgent.solveUsingGurobi();
			mathAgent.disposeUsingGurobi();
			double objvalue = mathAgent.getObjectiveValue();
			System.out.printf("Objective value=%.2f\n", objvalue);
			for (DecisionVariable dv : mathAgent.getDecisionVariables("x")) {
				System.out.printf("x%s=%.2f\n", dv.getIndices(), dv.getValue());
			}
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}

	private void solveUsingCplexLP(MathAgentGurobiCplex mathAgent) {
		try {
			mathAgent.prepareCPLEX();
			mathAgent.solveUsingCPLEX();
			mathAgent.computeDualsUsingCplex();
			List<String> dv_names = Arrays.asList("x0", "x1", "x2", "x3");
			double objvalue = mathAgent.getObjectiveValue();
			System.out.printf("Objective value=%.2f\n", objvalue);
			for (String dv : dv_names) {
				DecisionVariable decision_variable = mathAgent
						.getSingleDecisionVariable(dv);
				System.out
						.printf("%s=%.2f basis=%s rc=%.2f\n",
								dv,
								decision_variable.getValue(),
								mathAgent
										.getBasisStatusForVariableUsingCPLEX(decision_variable),
								decision_variable.getReducedCost());
			}
			for (int i = 0; i < mathAgent.getConstraints().size(); i++)
				System.out.printf("Dual value of constraint %d = %.2f\n", i,
						mathAgent.getDuals()[i]);
			mathAgent.disposeUsingCPLEX();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	private void solveUsingCplexIP(MathAgentGurobiCplex mathAgent) {
		try {
			mathAgent.prepareCPLEX();
			mathAgent.solveUsingCPLEX();
			mathAgent.disposeUsingCPLEX();
			double objvalue = mathAgent.getObjectiveValue();
			System.out.printf("Objective value=%.2f\n", objvalue);
			for (DecisionVariable dv : mathAgent.getDecisionVariables("x")) {
				System.out.printf("x%s=%.2f\n", dv.getIndices(), dv.getValue());
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	// demonstration of referencing to decision variables by variable names
	// singleDecisionVariable
	private MathAgentGurobiCplex getMathAgentForLP() {
		MathAgentGurobiCplex mathAgent = new MathAgentGurobiCplex();

		// variables generated one by one
		DecisionVariable x0 = new DecisionVariable("x0", "REAL");
		x0.setLb(0);
		mathAgent.addSingleDecisionVariable("x0", x0);
		DecisionVariable x1 = new DecisionVariable("x1", "REAL");
		x1.setLb(0);
		mathAgent.addSingleDecisionVariable("x1", x1);
		DecisionVariable x2 = new DecisionVariable("x2", "REAL");
		x2.setLb(0);
		mathAgent.addSingleDecisionVariable("x2", x2);
		DecisionVariable x3 = new DecisionVariable("x3", "REAL");
		mathAgent.addSingleDecisionVariable("x3", x3);
		x3.setLb(0);

		// objective
		LPExpression expr = new LPExpression();
		expr.addTerm(8.0, mathAgent.getSingleDecisionVariable("x0"));
		expr.addTerm(11.0, mathAgent.getSingleDecisionVariable("x1"));
		expr.addTerm(6.0, mathAgent.getSingleDecisionVariable("x2"));
		expr.addTerm(4.0, mathAgent.getSingleDecisionVariable("x3"));
		Objective objective = new Objective("Maximize");
		objective.setExpr(expr);
		mathAgent.setObjective(objective);

		// constraints
		LPExpression constr1 = new LPExpression();
		constr1.addTerm(5.0, mathAgent.getSingleDecisionVariable("x0"));
		constr1.addTerm(7.0, mathAgent.getSingleDecisionVariable("x1"));
		constr1.addTerm(4.0, mathAgent.getSingleDecisionVariable("x2"));
		constr1.addTerm(3.0, mathAgent.getSingleDecisionVariable("x3"));
		LPConstraint constraint = new LPConstraint("constraint1");
		constraint.setExpression(constr1);
		constraint.setLhs(-Double.MAX_VALUE);
		constraint.setRhs(14.0);
		mathAgent.addConstraint(constraint);
		mathAgent.setEnableOutput(false);
		return mathAgent;
	}

	// demonstration of referencing to decision variables by key and index
	// decisionVariable
	private MathAgentGurobiCplex getMathAgentForIP() {
		MathAgentGurobiCplex mathAgent = new MathAgentGurobiCplex();

		// variables
		List<DecisionVariable> dvs = new ArrayList<>();
		DecisionVariable x0 = new DecisionVariable("x0", "0", "BINARY");
		dvs.add(x0);
		DecisionVariable x1 = new DecisionVariable("x1", "1", "BINARY");
		dvs.add(x1);
		DecisionVariable x2 = new DecisionVariable("x2", "2", "BINARY");
		dvs.add(x2);
		DecisionVariable x3 = new DecisionVariable("x3", "3", "BINARY");
		dvs.add(x3);
		mathAgent.addIndexedDecisionVariables("x", dvs);

		// objective
		LPExpression expr = new LPExpression();
		expr.addTerm(8.0, mathAgent.getIndexedDecisionVariable("x", "0"));
		expr.addTerm(11.0, mathAgent.getIndexedDecisionVariable("x", "1"));
		expr.addTerm(6.0, mathAgent.getIndexedDecisionVariable("x", "2"));
		expr.addTerm(4.0, mathAgent.getIndexedDecisionVariable("x", "3"));
		Objective objective = new Objective("Maximize");
		objective.setExpr(expr);
		mathAgent.setObjective(objective);

		// constraints
		LPExpression constr1 = new LPExpression();
		constr1.addTerm(5.0, mathAgent.getIndexedDecisionVariable("x", "0"));
		constr1.addTerm(7.0, mathAgent.getIndexedDecisionVariable("x", "1"));
		constr1.addTerm(4.0, mathAgent.getIndexedDecisionVariable("x", "2"));
		constr1.addTerm(3.0, mathAgent.getIndexedDecisionVariable("x", "3"));
		LPConstraint constraint = new LPConstraint("constraint1");
		constraint.setExpression(constr1);
		constraint.setLhs(-Double.MAX_VALUE);
		constraint.setRhs(14.0);
		mathAgent.addConstraint(constraint);
		mathAgent.setEnableOutput(false);
		mathAgent.printProblemLPFormat();
		return mathAgent;
	}

}
