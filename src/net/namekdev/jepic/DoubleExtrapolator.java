package net.namekdev.jepic;

public class DoubleExtrapolator extends Extrapolator<Double> {
	public DoubleExtrapolator() {
		super(Double.class);
	}
	
	public DoubleExtrapolator(int size) {
		super(Double.class, size);
	}

	@Override
	protected Double set(Double immutableOut, Double val) {
		return val;
	}
	
	@Override
	protected Double add(Double immutableOut, Double val1, Double val2) {
		return val1 + val2;
	}

	@Override
	protected Double mult(Double immutableOut, Double val1, double val2) {
		return val1 * val2;
	}

	@Override
	protected Double subtract(Double immutableOut, Double val1, Double val2) {
		return val1 - val2;
	}

	@Override
	protected Double zero(Double immutableOut) {
		return 0.0;
	}

	@Override
	protected Double generateZero() {
		return 0.0;
	}
}
