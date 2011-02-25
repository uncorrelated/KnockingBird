package com.uncorrelated;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Timer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Bubble extends JFrame implements WindowListener {
	private Canvas canvas = null;
	private BufferedImage image = null;
	private Timer timer = null;
	private SwingImageTask sImage = new SwingImageTask();

	public Bubble() throws IOException {
		super("鳥が体をゆする");
		image = ImageIO.read(this.getClass().getResource("a_bird.jpg"));
		canvas = new ImgCanvas();
		setSize(image.getWidth() + 16, image.getHeight() + 64);
		addWindowListener(this);
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		sImage.setComponent(canvas);
		sImage.setRadius(64);
		sImage.setVectorY(22);
		sImage.setVectorX(2);
		setUI();
		setVisible(true);
		timer = new Timer();
		timer.scheduleAtFixedRate(sImage, 0, 50);
	}

	
	private class ImgCanvas extends Canvas {
		public void paint(Graphics g) {
			int ix = image.getWidth()/2 - 8;
			int iy = image.getHeight()/2 - 8;
			Dimension d = getSize();
			Image dbuf = createImage(d.width, d.height);
			Graphics gd = dbuf.getGraphics();
			sImage.setCenterX(ix);
			sImage.setCenterY(iy);
			gd.drawImage(sImage.transform(image), 0, 0, this);
//			gd.setColor(Color.yellow);
//			gd.drawArc(ix - radius, iy - radius, 2*radius, 2*radius, 0, 360);
			g.drawImage(dbuf, 0, 0, this);
		}
		public void update(Graphics g) {
			paint(g);
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
		timer.cancel();
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
