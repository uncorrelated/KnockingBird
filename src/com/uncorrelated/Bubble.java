package com.uncorrelated;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Bubble extends JFrame implements WindowListener, Runnable {
	private Canvas canvas = null;
	private JSpinner jspr1 = null;
	private volatile int move_x = 0, move_y = 0, radius = 65;
	private BufferedImage image = null;
	private Thread thread = null;
	private volatile boolean flag = true;

	public Bubble() throws IOException {
		super("鳥が体をゆする");
		image = ImageIO.read(this.getClass().getResource("a_bird.jpg"));
		canvas = new ImgCanvas();
		setSize(image.getWidth() + 16, image.getHeight() + 64);
		addWindowListener(this);
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);

		Container cnt = new Container();
		cnt.setLayout(new FlowLayout());

		jspr1 = new JSpinner(new SpinnerNumberModel(move_x, -100, 100, 1));
		jspr1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
		        JSpinner sp = (JSpinner)ce.getSource();
		        Number n = (Number)sp.getValue();
		        move_x = n.intValue();
		        canvas.repaint();
			}
		});

		cnt.add(new JLabel("歪み"));
		cnt.add(jspr1);
		add(cnt, BorderLayout.SOUTH);

		thread = new Thread(this);
		thread.start();

		setUI();
		setVisible(true);
	}

	private int range(int n, int min, int max){
		if(n<min)
			return min;
		if(n>max)
			return max;
		return n;
	}
	
	private BufferedImage transform(BufferedImage src, int cx, int cy, int r, int mx, int my){
		int w = src.getWidth();
		int h = src.getHeight();
		BufferedImage dst = new BufferedImage(w, h, src.getType());
		int[] bms = src.getRGB(0, 0, w, h, null, 0, w);
		int[] bmd = new int[bms.length];
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){
				int dx = x - cx;
				int dy = y - cy;
				double len = Math.sqrt(dx*dx + dy*dy);
				double frc = len/r;
				double eff = 1 > frc ? Math.sqrt(1 - frc):0;
				if(eff<0)
					eff = 0;
				int sx = range(x + (int)(eff*mx), 0, w - 1);
				int sy = range(y + (int)(eff*my), 0, h - 1);
				int ptr_dst = x + w*y;
				int ptr_src = sx + w*sy;
				bmd[ptr_dst] = bms[ptr_dst];
				bmd[ptr_dst] = bms[ptr_src];
			}
		}
		dst.setRGB(0, 0, w, h, bmd, 0, w);
		return dst;
	}
	
	private class ImgCanvas extends Canvas {
		public void paint(Graphics g) {
			int ix = image.getWidth()/2 - 8;
			int iy = image.getHeight()/2 - 8;
			Dimension d = getSize();
			Image dbuf = createImage(d.width, d.height);
			Graphics gd = dbuf.getGraphics();
			gd.drawImage(transform(image, ix, iy, radius, move_x, move_y), 0, 0, this);
//			gd.setColor(Color.yellow);
//			gd.drawArc(ix - radius, iy - radius, 2*radius, 2*radius, 0, 360);
			g.drawImage(dbuf, 0, 0, this);
		}
		public void update(Graphics g) {
			paint(g);
		}
	}

	private int span = 20;
	private int y_direction = 2;
	private void knock(){
		int speed = 3;
        move_y += y_direction;
        if(span/2<move_y){
        	y_direction = -1*speed;
        } else if(move_y < -1*span/2) {
        	y_direction = speed;
        }
	}

	private long calcWait(){
		return 20 + 50*Math.abs(move_y)/span;
	}
	
	public void run() {
		while (flag) {
			try {
				synchronized (thread) {
					thread.wait(calcWait());
				}
			} catch (InterruptedException e) {
			}
			knock();
			canvas.repaint();
		}
	}

	private void setUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		flag = false;
		synchronized (thread) {
			thread.notify();
		}
		try {
			thread.join(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.exit(0);
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public static void main(String[] args) throws IOException {
		Bubble bubble = new Bubble();
	}

}
