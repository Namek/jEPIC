package net.namekdev.jepic;

public class FloatExtrapolator extends Extrapolator<Float> {
	public FloatExtrapolator() {
		super(Float.class);
	}
	
	public FloatExtrapolator(int size) {
		super(Float.class, size);
	}

	@Override
	protected Float set(Float immutableOut, Float val) {
		return val;
	}
	
	@Override
	protected Float add(Float immutableOut, Float val1, Float val2) {
		return val1 + val2;
	}

	@Override
	protected Float mult(Float immutableOut, Float val1, double val2) {
		return (float)(val1.doubleValue() * val2);
	}

	@Override
	protected Float subtract(Float immutableOut, Float val1, Float val2) {
		return val1 - val2;
	}

	@Override
	protected Float zero(Float immutableOut) {
		return 0f;
	}

	@Override
	protected Float generateZero() {
		return 0f;
	}
}