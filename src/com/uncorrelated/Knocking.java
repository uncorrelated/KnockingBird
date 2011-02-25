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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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

public class Knocking extends JFrame implements WindowListener {
	private Canvas canvas = null;
	private BufferedImage image = null;
	private Timer timer = null;
	private SwingImageTask sImage = new SwingImageTask();

	public Knocking() throws IOException {
		super("鳥が体をゆする");
		image = ImageIO.read(this.getClass().getResource("a_bird.jpg"));
		canvas = new ImgCanvas();
		setSize(image.getWidth() + 16, image.getHeight() + 64);
		addWindowListener(this);
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		sImage.setComponent(canvas);
		setUI();
		setVisible(true);
		timer = new Timer();
		timer.scheduleAtFixedRate(sImage, 0, 50);
	}

	
	private class ImgCanvas extends Canvas implements MouseListener,MouseMotionListener {
		private Point mpp = null, mrp = null, mmp = null;
		public ImgCanvas(){
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		public void paint(Graphics g) {
			Dimension d = getSize();
			Image dbuf = createImage(d.width, d.height);
			Graphics gd = dbuf.getGraphics();
			gd.drawImage(sImage.transform(image), 0, 0, this);
			if(null!=mpp && null!=mmp){
				int radius = length(mpp, mmp);
				gd.setColor(Color.black);
				drawCircle(gd, mpp.x, mpp.y, radius - 1);
				drawCircle(gd, mpp.x, mpp.y, radius + 1);
				gd.setColor(Color.yellow);
				drawCircle(gd, mpp.x, mpp.y, radius);
				gd.setColor(Color.black);
				gd.drawLine(mpp.x + 1, mpp.y, mmp.x + 1, mmp.y);
				gd.drawLine(mpp.x - 1, mpp.y, mmp.x - 1, mmp.y);
				gd.setColor(Color.yellow);
				gd.drawLine(mpp.x, mpp.y, mmp.x, mmp.y);
			}
			g.drawImage(dbuf, 0, 0, this);
		}
		private int length(Point p1, Point p2){
			int dx = p1.x - p2.x;
			int dy = p1.y - p2.y;
			return (int)Math.sqrt(dx*dx + dy*dy);
		}
		private void drawCircle(Graphics g, int x, int y, int r){
			g.drawArc(x - r, y - r, 2*r, 2*r, 0, 360);
		}
		public void update(Graphics g) {
			paint(g);
		}
		public void mouseClicked(MouseEvent arg0) {
		}
		public void mouseEntered(MouseEvent arg0) {
		}
		public void mouseExited(MouseEvent arg0) {
		}
		public void mousePressed(MouseEvent arg0) {
			mpp = arg0.getPoint();
		}
		public void mouseReleased(MouseEvent arg0) {
			mrp = arg0.getPoint();
			sImage.setCenterX(mpp.x);
			sImage.setCenterY(mpp.y);
			int r = length(mpp, mrp);
			int power = 22;
			int vx = 0 < r ? power*Math.abs(mrp.x - mpp.x)/r : 0;
			int vy = 0 < r ? power*Math.abs(mrp.y - mpp.y)/r : 0;
			sImage.setRadius(r);
			sImage.setVector(vx, vy);
			mrp = mpp = mmp = null;
		}
		public void mouseDragged(MouseEvent e) {
			mmp = e.getPoint();
		}
		public void mouseMoved(MouseEvent e) {
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
		Knocking bubble = new Knocking();
	}
}
