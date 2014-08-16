package net.namekdev.jepic;

import java.lang.reflect.Array;

public abstract class Extrapolator<T> {
	private T[] _snapPos, _snapVel, _aimPos, _lastPacketPos;
	private T[] _tmpArr, _tmpArr2, _tmpArr3;
	private double _snapTime, _aimTime, _lastPacketTime, _latency, _updateTime;
	private Class<T> _type;
	private int _size;
	
	
	public Extrapolator(Class<T> type) {
		this(type, type == Number.class ? 2 : 1);
	}
	
	public Extrapolator(Class<T> type, int size) {
		_type = type;
		_size = size;
		_snapPos = createArray();
		_snapVel = createArray();
		_aimPos = createArray();
		_lastPacketPos = createArray();
		_tmpArr = createArray();
		_tmpArr2 = createArray();
		_tmpArr3 = createArray();
	}
	
	public boolean addSample(double packetTime, double curTime, T[] pos) {
		// The best guess I can make for velocity is the difference between
		// this sample and the last registered sample.
		T[] vel = _tmpArr2;
		if (Math.abs(packetTime - _lastPacketTime) > 1e-4) {
			double dt = 1.0 / (packetTime - _lastPacketTime);
			for (int i = 0; i < _size; ++i) {
				// vel[i] = (pos[i] - _lastPacket[i]) * dt
				vel[i] = mult(vel[i], subtract(_tmpArr3[i], pos[i], _lastPacketPos[i]), dt);
			}
		}
		else {
			clear(vel);
		}
		
		return addSample(packetTime, curTime, pos, vel);
	}
	
	public boolean addSample(double packetTime, double curTime, T[] pos, T[] vel) {
		if (!estimates(packetTime, curTime)) {
			return false;
		}
		
		copyArray(_lastPacketPos, pos);
		_lastPacketTime = packetTime;
		readPosition(curTime, _snapPos);
		_aimTime = curTime + _updateTime;
		double dt = _aimTime - packetTime;
		_snapTime = curTime;
		for (int i = 0; i < _size; ++i) {
			// _aimPos[i] = pos[i] + vel[i] * dt
			_aimPos[i] = add(_aimPos[i], pos[i], mult(_tmpArr3[i], vel[i], dt));
		}
		
		// I now have two positions and two times:
		//   1. _aimPos / _aimTime
		//   2. _snapPos / _snapTime
		// I must generate the interpolation velocity based on these two samples.
		// However, if _aimTime is the same as _snapTime, I'm in trouble.
		// In that case, use the supplied velocity.
		if (Math.abs(_aimTime - _snapTime) < 1e-4) {
			copyArray(_snapVel, vel);
		}
		else {
			dt = 1.0 / (_aimTime - _snapTime);
			for (int i = 0; i < _size; ++i) {
				// _snapVel[i] = (_aimPos[i] - _snapPos[i]) * dt
				_snapVel[i] = mult(_snapVel[i], subtract(_tmpArr3[i], _aimPos[i], _snapPos[i]), dt);
			}
		}
		
		return true;
	}
	
	/**
	 * Version for extrapolator of {@code size = 1}.
	 */
	public boolean addSample(double packetTime, double curTime, T pos) {
		checkArraySizeOne();
		
		_tmpArr[0] = set(_tmpArr[0], pos);
		return addSample(packetTime, curTime, _tmpArr);
	}
	
	/**
	 * Version for extrapolator of {@code size = 1}.
	 */
	public boolean addSample(double packetTime, double curTime, T pos, T vel) {
		checkArraySizeOne();
		
		_tmpArr[0] = set(_tmpArr[0], pos);
		_tmpArr2[0] = set(_tmpArr2[0], vel);
		return addSample(packetTime, curTime, _tmpArr, _tmpArr2);
	}
	
	public void reset(double packetTime, double curTime, T[] pos) {
		reset(packetTime, curTime, pos, clear(_tmpArr));
	}
	
	public void reset(double packetTime, double curTime, T[] pos, T[] vel) {
		assert(packetTime <= curTime);
		
		_lastPacketTime = packetTime;
		copyArray(_lastPacketPos, pos);
		_snapTime = curTime;
		copyArray(_snapPos, pos);
		_updateTime = curTime - packetTime;
		_latency = _updateTime;
		_aimTime = curTime + _updateTime;
		copyArray(_snapVel, vel);
	
		for (int i = 0; i < _size; ++i) {
			// _aimPos[i] = _snapPos[i] + _snapVel[i] * _updateTime
			_aimPos[i] = add(_aimPos[i], _snapPos[i], mult(_tmpArr3[i], _snapVel[i], _updateTime));
		}
	}
	
	/**
	 * Version for extrapolator of {@code size = 1}.
	 */
	public void reset(double packetTime, double curTime, T pos) {
		checkArraySizeOne();
		
		reset(packetTime, curTime, pos, generateZero());
	}
	
	/**
	 * Version for extrapolator of {@code size = 1}.
	 */
	public void reset(double packetTime, double curTime, T pos, T vel) {
		checkArraySizeOne();
		
		assert(packetTime <= curTime);
		
		_lastPacketTime = packetTime;
		_lastPacketPos[0] = set(_lastPacketPos[0], pos);
		_snapTime = curTime;
		_snapPos[0] = set(_snapPos[0], pos);
		_updateTime = curTime - packetTime;
		_latency = _updateTime;
		_aimTime = curTime + _updateTime;
		_snapVel[0] = set(_snapVel[0], vel);
		_aimPos[0] = add(_aimPos[0], _snapPos[0], mult(_tmpArr3[0], _snapVel[0], _updateTime));
	}

	public boolean readPosition(double forTime, T[] outPos) {
		return readPosition(forTime, outPos, null);
	}
	
	public boolean readPosition(double forTime, T[] outPos, T[] outVel) {
		boolean isOk = true;
		
		// asking for something before allowable time?
		if (forTime < _snapTime) {
			forTime = _snapTime;
			isOk = false;
		}
		
		// asking for something very far in the future?
		double maxRange = _aimTime + _updateTime;
		if (forTime > maxRange) {
			forTime = maxRange;
			isOk = false;
		}
		
		// calculate the interpolated position
		for (int i = 0; i < _size; ++i) {
			if (outVel != null) {
				outVel[i] = set(outVel[i], _snapVel[i]);
			}
			
			// outPos[i] = _snapPos[i] + _snapVel[i] * (forTime - _snapTime)
			outPos[i] = add(outPos[i], _snapPos[i], mult(_tmpArr3[i], _snapVel[i], forTime - _snapTime));
		}
		
		if (!isOk && outVel != null) {
			clear(outVel);
		}
		
		return isOk;
	}
	
	/**
	 * Version for extrapolator of {@code size = 1}.
	 */
	public T readPosition(double forTime) {
		checkArraySizeOne();
		
		if (readPosition(forTime, _tmpArr)) {
			return _tmpArr[0];
		}
		
		return null;
	}
	
	public double estimateLatency() {
		return _latency;
	}
	
	public double estimateUpdateTime() {
		return _updateTime;
	}
	
	private boolean estimates(double packet, double cur) {
		if (packet <= _lastPacketTime) {
			return false;
		}
		
		// The theory is that, if latency increases, quickly
		// compensate for it, but if latency decreases, be a
		// little more resilient; this is intended to compensate
		// for jittery delivery.
		double lat = cur - packet;
		if (lat < 0) {
			lat = 0;
		}
		if (lat > _latency) {
			_latency = (_latency + lat) * 0.5;
		}
		else {
			_latency = (_latency * 7 + lat) * 0.125;
		}
		
		// Do the same running average for update time.
		// Again, the theory is that a lossy connection wants
		// an average of a higher update time.
		double tick = packet - _lastPacketTime;
		if (tick > _updateTime) {
			_updateTime = (_updateTime + tick) * 0.5;
		}
		else {
			_updateTime = (_updateTime * 7 + tick) * 0.125;
		}
		
		return true;
	}

	/**
	 * Setup {@code out} variable if {@code T} is a reference type.
	 * This reference (or value) should be also returned. 
	 */
	protected abstract T set(T out, T val);
	
	/**
	 * Setup {@code out} variable if {@code T} is a reference type.
	 * This reference (or value) should be also returned. 
	 */
	protected abstract T add(T out, T val1, T val2);

	/**
	 * Setup {@code out} variable if {@code T} is a reference type.
	 * This reference (or value) should be also returned. 
	 */
	protected abstract T mult(T out, T val1, double val2);
	
	/**
	 * Setup {@code out} variable if {@code T} is a reference type.
	 * This reference (or value) should be also returned. 
	 */
	protected abstract T subtract(T out, T val1, T val2);
	
	/**
	 * Setup {@code out} variable if {@code T} is a reference type.
	 * This reference (or value) should be also returned. 
	 */
	protected abstract T zero(T out);
	
	/**
	 * Always return new object of default (zero) value.
	 */
	protected abstract T generateZero();
	
	
	private T[] clear(T[] arr) {
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = arr[i] == null ? generateZero() : zero(arr[i]);
		}
		return arr;
	}
	
	@SuppressWarnings("unchecked")
	private T[] createArray() {
		return clear((T[]) Array.newInstance(_type, _size));
	}
	
	private void copyArray(T[] dest, T[] src) {
		for (int i = 0, n = src.length; i < n; ++i) {
			dest[i] = set(dest[i], src[i]);
		}
	}
	
	private void checkArraySizeOne() {
		if (_size != 1) {
			throw new UnsupportedOperationException("This function should be called only when size = 1!");
		}
	}
}
