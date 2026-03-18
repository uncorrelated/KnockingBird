package com.uncorrelated.kbird;

import java.awt.image.BufferedImage;

public class SwingBufferedImage implements java.lang.Cloneable {
	private volatile int centerX = 0, centerY = 0, Radius1 = 0, Radius2 = 0, vectorX,
			vectorY, span = 1, deformation = 1, direction = 1, baseSpeed = 5,
			power = 0;
	private volatile double coefficient = 1;
	private volatile int count = 0, baseCount = 1;
	private volatile boolean IsDecline = true, IsSuspend = false;

	private int speed() {
		int speed = (int) (span * decline() * baseSpeed / 100);
		if (speed <= 0)
			speed = 1;
		return speed;
	}

	public float decline() {
		float decline = (float) count / baseCount;
		return decline;
	}

	public void decreaseCount() {
		if (IsDecline && 0 < count) {
			if (0 == --count){
				deformation = 0;
				reset();
			}
		}
	}

	public void move() {
		if (0 >= count || IsSuspend) {
			return;
		}
		float decline = decline();
		deformation += direction * speed();
		int u_limit = (int) (decline * span / 2);
		int l_limit = (int) (decline * -1 * span / 2);
		if (u_limit < deformation) {
			direction = -1;
			decreaseCount();
		} else if (deformation < l_limit) {
			direction = 1;
			decreaseCount();
		}
		if (0 == deformation)
			move();
	}

	private int range(int n, int min, int max) {
		if (n < min)
			return min;
		if (n > max)
			return max;
		return n;
	}

	private int[] transform(int[] bms, int w, int h, int cx, int cy, int r1, int r2,
			int mx, int my, double coefficient) {
		int[] bmd = new int[bms.length];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int ptr_dst = x + w * y;
				int dx = x - cx;
				int dy = y - cy;
				double frc = (float)dx*dx/r1/r1 + (float)dy*dy/r2/r2;
				if (1 > frc) {
					double eff = 1 > frc ? Math.pow(1 - frc, coefficient) : 0;
					double ex = eff * mx;
					double ey = eff * my;
					int fx = (int) Math.floor(ex);
					int fy = (int) Math.floor(ey);
					int sx1 = range(x + fx, 0, w - 1);
					int sy1 = range(y + fy, 0, h - 1);
					int sx2 = range(sx1 + 1, 0, w - 1);
					int sy2 = range(sy1 + 1, 0, h - 1);
					double a = ex - fx;
					double b = ey - fy;
					bmd[ptr_dst] = mean(bms[sx1 + w * sy1], bms[sx1 + w * sy2],
							bms[sx2 + w * sy1], bms[sx2 + w * sy2], a, b);
				} else {
					bmd[ptr_dst] = bms[ptr_dst];
				}
			}
		}
		return bmd;
	}	

	private int mean(int p11, int p12, int p21, int p22, double a, double b) {
		double r11 = (1 - a) * (1 - b);
		double r12 = a * (1 - b);
		double r21 = (1 - a) * b;
		double r22 = a * b;
		int alpha = 0xff000000 & (int) (r11 * (0xff000000 & p11) + r12
				* (0xff000000 & p12) + r21 * (0xff000000 & p21) + r22
				* (0xff000000 & p22));
		int red = 0x00ff0000 & (int) (r11 * (0x00ff0000 & p11) + r12
				* (0x00ff0000 & p12) + r21 * (0x00ff0000 & p21) + r22
				* (0x00ff0000 & p22));
		int green = 0x0000ff00 & (int) (r11 * (0x0000ff00 & p11) + r12
				* (0x0000ff00 & p12) + r21 * (0x0000ff00 & p21) + r22
				* (0x0000ff00 & p22));
		int blue = 0x000000ff & (int) (r11 * (0x000000ff & p11) + r12
				* (0x000000ff & p12) + r21 * (0x000000ff & p21) + r22
				* (0x000000ff & p22));
		return alpha | red | green | blue;
	}

	private BufferedImage transform(BufferedImage src, int cx, int cy, int r1, int r2,
			int mx, int my, double coefficient) {
		int w = src.getWidth();
		int h = src.getHeight();
		BufferedImage dst = new BufferedImage(w, h, src.getType());
		dst.setRGB(0, 0, w, h, transform(src.getRGB(0, 0, w, h, null, 0, w), w, h, cx, cy, r1, r2, mx, my, coefficient), 0, w);
		return dst;
	}

	public BufferedImage transform(BufferedImage src) {
		if (0 >= Radius1)
			return src;
		return transform(src, centerX, centerY, Radius1, Radius2, vectorX * deformation
				/ span, vectorY * deformation / span, coefficient);
	}

	public int[] transform(int[] bms, int w, int h) {
		if (0 >= Radius1)
			return bms;
		return transform(bms, w, h, centerX, centerY, Radius1, Radius2, vectorX * deformation
				/ span, vectorY * deformation / span, coefficient);
	}
	
	public int getCenterX() {
		return centerX;
	}

	public void setCenterX(int centerX) {
		this.centerX = centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	public void setCenterY(int centerY) {
		this.centerY = centerY;
	}

	public void setRadius(int r1, int r2) {
		Radius1 = r1;
		Radius2 = r2;
	}

	public int getRadius1() {
		return Radius1;
	}

	public int getRadius2() {
		return Radius2;
	}

	public int getVectorX() {
		return vectorX;
	}

	private void setup() {
		deformation = 0;
		span = (int) Math.sqrt(vectorX * vectorX + vectorY * vectorY);
		if (0 >= span)
			span = 1;
		direction = 1;
		baseCount = count = span * speed();
	}

	public void reset() {
		vectorX = 0;
		vectorY = 0;
		Radius1 = 0;
		Radius2 = 0;
		deformation = 0;
		count = 0;
	}

	public void setVectorX(int vectorX) {
		this.vectorX = vectorX;
		setup();
	}

	public int getVectorY() {
		return vectorY;
	}

	public void setVectorY(int vectorY) {
		this.vectorY = vectorY;
		setup();
	}

	public void setVector(int vectorX, int vectorY) {
		this.vectorX = vectorX;
		this.vectorY = vectorY;
		setup();
	}

	public void setSpeed(int arg) {
		baseSpeed = arg;
	}

	public boolean isDecline() {
		return IsDecline;
	}

	public void setDecline(boolean d) {
		IsDecline = d;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public void changePower(int p) {
		if (0 < power) {
			span = p * span / power;
			if (0 >= span)
				span = 1;
			power = p;
		}
	}

	public double getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(double coefficient) {
		this.coefficient = coefficient;
	}

	public boolean isSuspend() {
		return IsSuspend;
	}

	public void setSuspend(boolean f) {
		IsSuspend = f;
	}
	public boolean toggleSuspend() {
		return IsSuspend = !IsSuspend;
	}

	public SwingBufferedImage clone() {
		try {
			return (SwingBufferedImage)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
