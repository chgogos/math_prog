package mathmodeler;

public class DecisionVariable {
	String name;
	String indices;
	double lb = -Double.MAX_VALUE;
	double ub = Double.MAX_VALUE;
	String type;
	double value;
	double reducedCost;

	public DecisionVariable(String name, String indices, String type) {
		super();
		this.name = name;
		this.indices = indices;
		this.type = type;
		if (type.equalsIgnoreCase("BINARY")) {
			this.lb = 0;
			this.ub = 1;
		}
	}

	public DecisionVariable(String name, String type) {
		super();
		this.name = name;
		this.indices = "NO INDEX";
		this.type = type;
		if (type.equalsIgnoreCase("BINARY")) {
			this.lb = 0;
			this.ub = 1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((indices == null) ? 0 : indices.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		DecisionVariable other = (DecisionVariable) obj;
		if (indices == null) {
			if (other.indices != null)
				return false;
		} else if (!indices.equals(other.indices))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s %s", name, type);
	}

	public void setLb(double lb) {
		this.lb = lb;
	}

	public void setUb(double ub) {
		this.ub = ub;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getIndices() {
		return indices;
	}

	public double getReducedCost() {
		return reducedCost;
	}

	public void setReducedCost(double reducedCost) {
		this.reducedCost = reducedCost;
	}

	public String getName() {
		return name;
	}

	public double getLb() {
		return lb;
	}

	public double getUb() {
		return ub;
	}

	public String getType() {
		return type;
	}

}
