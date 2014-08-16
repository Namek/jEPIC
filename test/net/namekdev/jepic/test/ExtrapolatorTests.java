package net.namekdev.jepic.test;

import static org.junit.Assert.*;
import net.namekdev.jepic.DoubleExtrapolator;
import net.namekdev.jepic.Extrapolator;
import net.namekdev.jepic.FloatExtrapolator;

import org.junit.Test;

public class ExtrapolatorTests {
	@Test
	public void basicUsageTest() {
		final DoubleExtrapolator i = new DoubleExtrapolator(2);
		Double p[] = { 0.0, 0.0 };
		Double f[];
		
		i.reset(0.1, 0.1, p);
		i.readPosition(0.1, p);
		assertTrue(p[0] == 0 && p[1] == 0);
		assertTrue(!i.addSample(0, 1, p));
		f = new Double[] { 1.0, 0.0 };
		assertTrue(i.addSample(1, 1.5, f));
		assertTrue(i.estimateLatency() < 0.5f);
		assertTrue(i.estimateLatency() > 0.1f);
		assertTrue(i.estimateUpdateTime() > 0.4f);
		assertTrue(i.estimateUpdateTime() < 1.0f);
		f = new Double[] { 2.0, 0.0 };
		assertTrue(i.addSample(1.5, 2, f));
		i.readPosition(2, p);
		assertTrue(p != null);
		assertTrue(Math.abs(2.5f - p[0]) < 0.25);
		assertTrue(i.estimateLatency() < 0.5f);
		assertTrue(i.estimateLatency() > 0.3f);
		assertTrue(i.estimateUpdateTime() > 0.4f);
		assertTrue(i.estimateUpdateTime() < 0.6f);
		f = new Double[] { 3.0, 0.0 };
		assertTrue(i.addSample(2, 2.5, f));
		i.readPosition(2.5, p);
		assertTrue(p != null);
		assertTrue(Math.abs(4 - p[0]) < 0.125);
		f = new Double[] { 4.0, 0.0 };
		assertTrue(i.addSample(2.5, 3, f));
		i.readPosition(3, p);
		assertTrue(p != null);
		assertTrue(Math.abs(5 - p[0]) < 0.07);
		i.readPosition(3.25, p);
		assertTrue(p != null);
		assertTrue(Math.abs(5.5 - p[0]) < 0.07);
		//  don't allow extrapolation too far forward
		assertTrue(!i.readPosition(4, p));
	}
	
	@Test
	public void oneFloatExtrapolatorTest() {
		final FloatExtrapolator i = new FloatExtrapolator(1);
		float f = 0;
		Float p;
		i.reset(0.1, 0.1, f);
		p = i.readPosition(0.1);
		assertTrue(p != null);
		assertTrue(p == 0);
		assertTrue(!i.addSample(0, 1, p));
		f = 1;
		assertTrue(i.addSample(1, 1.5, f));
		assertTrue(i.estimateLatency() < 0.5f);
		assertTrue(i.estimateLatency() > 0.1f);
		assertTrue(i.estimateUpdateTime() > 0.4f);
		assertTrue(i.estimateUpdateTime() < 1.0f);
		f = 2;
		assertTrue(i.addSample(1.5, 2, f));
		p = i.readPosition(2);
		assertTrue(p != null);
		assertTrue(Math.abs(2.5f - p) < 0.25);
		assertTrue(i.estimateLatency() < 0.5f);
		assertTrue(i.estimateLatency() > 0.3f);
		assertTrue(i.estimateUpdateTime() > 0.4f);
		assertTrue(i.estimateUpdateTime() < 0.6f);
		f = 3;
		assertTrue(i.addSample(2, 2.5, f));
		p = i.readPosition(2.5);
		assertTrue(p != null);
		assertTrue(Math.abs(4 - p) < 0.125);
		f = 4;
		assertTrue(i.addSample(2.5, 3, f));
		p = i.readPosition(3);
		assertTrue(p != null);
		assertTrue(Math.abs(5 - p) < 0.07);
		p = i.readPosition(3.25);
		assertTrue(p != null);
		assertTrue(Math.abs(5.5 - p) < 0.07);
		//  don't allow extrapolation too far forward
		p = i.readPosition(4);
		assertTrue(p == null);
	}
	
	@Test
	public void customExtrapolatorTest() {
		final Extrapolator<Point1> i = new Point1Extrapolator();
		
		Point1 f = new Point1(0);
		Point1 p;
		i.reset(0.1, 0.1, f);
		p = i.readPosition(0.1);
		assertTrue(p != null);
		assertTrue(p.x == 0);
		assertTrue(!i.addSample(0, 1, p));
		f.x = 1;
		assertTrue(i.addSample(1, 1.5, f));
		assertTrue(i.estimateLatency() < 0.5f);
		assertTrue(i.estimateLatency() > 0.1f);
		assertTrue(i.estimateUpdateTime() > 0.4f);
		assertTrue(i.estimateUpdateTime() < 1.0f);
		f.x = 2;
		assertTrue(i.addSample(1.5, 2, f));
		p = i.readPosition(2);
		assertTrue(p != null);
		assertTrue(Math.abs(2.5f - p.x) < 0.25);
		assertTrue(i.estimateLatency() < 0.5f);
		assertTrue(i.estimateLatency() > 0.3f);
		assertTrue(i.estimateUpdateTime() > 0.4f);
		assertTrue(i.estimateUpdateTime() < 0.6f);
		f.x = 3;
		assertTrue(i.addSample(2, 2.5, f));
		p = i.readPosition(2.5);
		assertTrue(p != null);
		assertTrue(Math.abs(4 - p.x) < 0.125);
		f.x = 4;
		assertTrue(i.addSample(2.5, 3, f));
		p = i.readPosition(3);
		assertTrue(p != null);
		assertTrue(Math.abs(5 - p.x) < 0.07);
		p = i.readPosition(3.25);
		assertTrue(p != null);
		assertTrue(Math.abs(5.5 - p.x) < 0.07);
		//  don't allow extrapolation too far forward
		p = i.readPosition(4);
		assertTrue(p == null);
	}
	
	static class Point1 {
		float x;
		
		public Point1(float x) {
			this.x = x;
		}
	}
	
	static class Point1Extrapolator extends Extrapolator<Point1> {
		public Point1Extrapolator() {
			super(Point1.class);
		}

		@Override
		protected Point1 set(Point1 out, Point1 val) {
			out.x = val.x;
			return out;
		}

		@Override
		protected Point1 add(Point1 out, Point1 el1, Point1 el2) {
			out.x = el1.x + el2.x;
			return out;
		}

		@Override
		protected Point1 mult(Point1 out, Point1 el1, double el2) {
			out.x = el1.x * (float)el2;
			return out;
		}

		@Override
		protected Point1 subtract(Point1 out, Point1 el1, Point1 el2) {
			out.x = el1.x - el2.x;
			return out;
		}

		@Override
		protected Point1 zero(Point1 out) {
			out.x = 0;
			return out;
		}

		@Override
		protected Point1 generateZero() {
			return new Point1(0);
		}
	}
}
