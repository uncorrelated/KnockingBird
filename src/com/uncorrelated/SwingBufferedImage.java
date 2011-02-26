package com.uncorrelated;

import java.awt.image.BufferedImage;

public class SwingBufferedImage {
	private volatile int centerX = 0, centerY = 0, Radius = 0, vectorX,
			vectorY, span = 1, deformation = 1, direction = 1, baseSpeed = 5, power = 0;
	private volatile int count=0, baseCount=1;
	private volatile boolean IsDecline = true;

	private int speed(){
		int speed = baseSpeed + baseSpeed * Math.abs(deformation) / span;
		return speed;
	}

	public float decline(){
		float decline = (float)count/baseCount;
		return decline*decline;
	}
	
	public void decreaseCount(){
		if(IsDecline && 0<count){
			if(0 == --count)
				deformation = 0;
		}
	}
	
	public void move() {
		if (0>=count){
			return;
		}
		float decline = decline();
		float f = decline * direction * speed();
		deformation += (int)(0<f ? Math.ceil(f) : Math.floor(f));
		int u_limit = (int)(decline * span / 2);
		int l_limit = (int)(decline * -1 * span / 2);
		if (u_limit < deformation) {
			direction = -1;
			decreaseCount();
		} else if (deformation < l_limit) {
			direction = 1;
			decreaseCount();
		}
	}

	private int range(int n, int min, int max) {
		if (n < min)
			return min;
		if (n > max)
			return max;
		return n;
	}

	private BufferedImage transform(BufferedImage src, int cx, int cy, int r,
			int mx, int my) {
		int w = src.getWidth();
		int h = src.getHeight();
		BufferedImage dst = new BufferedImage(w, h, src.getType());
		int[] bms = src.getRGB(0, 0, w, h, null, 0, w);
		int[] bmd = new int[bms.length];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int dx = x - cx;
				int dy = y - cy;
				double len = Math.sqrt(dx * dx + dy * dy);
				double frc = len / r;
				double eff = 1 > frc ? Math.sqrt(1 - frc) : 0;
				if (eff < 0)
					eff = 0;
				int sx = range(x + (int) (eff * mx), 0, w - 1);
				int sy = range(y + (int) (eff * my), 0, h - 1);
				int ptr_dst = x + w * y;
				int ptr_src = sx + w * sy;
				bmd[ptr_dst] = bms[ptr_dst];
				bmd[ptr_dst] = bms[ptr_src];
			}
		}
		dst.setRGB(0, 0, w, h, bmd, 0, w);
		return dst;
	}

	public BufferedImage transform(BufferedImage src) {
		return transform(src, centerX, centerY, Radius, vectorX * deformation
				/ span, vectorY * deformation / span);
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

	public int getRadius() {
		return Radius;
	}

	public void setRadius(int radius) {
		Radius = radius;
	}

	public int getVectorX() {
		return vectorX;
	}

	private void setup(){
		deformation = 0;
		span = (int)Math.sqrt(vectorX*vectorX + vectorY*vectorY);
		if(0>=span)
			span = 1;
		direction = vectorX*vectorY <0 ? -1 : 1;
		baseCount = count = span*speed();
	}
	
	public void reset(){
		vectorX = 0;
		vectorY = 0;
		Radius = 0;
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

	public void setSpeed(int arg){
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
	
	public void changePower(int p){
		if(0<power){
			span = p*span/power;
			power = p;
		}
	}
}
