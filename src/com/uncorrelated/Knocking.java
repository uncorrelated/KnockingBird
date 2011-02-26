package com.uncorrelated;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Knocking extends JFrame implements WindowListener {
	private Canvas canvas = null;
	private BufferedImage image = null;
	private Timer timer = null;
	private SwingImageTask siTask = new SwingImageTask();
	private JSlider jsl1, jsl2;

	public Knocking() throws IOException {
		super("鳥が体をゆする");
		image = ImageIO.read(this.getClass().getResource("a_bird.jpg"));
		setSize(320, 320);
		setLocationRelativeTo(null);
		addWindowListener(this);
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.ipady = 8;
		gbc1.gridx = gbc1.gridy = 0;
		gbc1.gridwidth = 3;
		canvas = new ImgCanvas();
		canvas.setSize(image.getWidth(), image.getHeight());
		gbl.setConstraints(canvas, gbc1);
		add(canvas);
		siTask.setComponent(canvas);
		
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 0;
		gbc2.gridy = 1;
		JLabel jl1 = new JLabel("揺れの大きさ");
		gbl.setConstraints(jl1, gbc2);
		add(jl1);
		
		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.gridx = 1;
		gbc3.gridy = 1;
		gbc3.gridwidth = 2;
		jsl1 = new JSlider(3, 40, 20);
		gbl.setConstraints(jsl1, gbc3);
		add(jsl1);
		
		GridBagConstraints gbc4 = new GridBagConstraints();
		gbc4.gridx = 0;
		gbc4.gridy = 2;
		JLabel jl2 = new JLabel("揺れの速さ");
		gbl.setConstraints(jl2, gbc4);
		add(jl2);
		
		GridBagConstraints gbc5 = new GridBagConstraints();
		gbc5.gridx = 1;
		gbc5.gridy = 2;
		gbc5.gridwidth = 2;
		jsl2 = new JSlider(1, 10, 5);
		gbl.setConstraints(jsl2, gbc5);
		add(jsl2);
		
		setUI();
		setVisible(true);
		timer = new Timer();
		timer.scheduleAtFixedRate(siTask, 0, 50);
	}

	
	private class ImgCanvas extends Canvas implements MouseListener,MouseMotionListener {
		private Point mpp = null, mrp = null, mmp = null;
		public ImgCanvas(){
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		public void paint(Graphics g) {
			setSize(image.getWidth(), image.getHeight());
			Image dbuf = createImage(image.getWidth(), image.getHeight());
			Graphics gd = dbuf.getGraphics();
			gd.drawImage(siTask.transform(image), 0, 0, this);
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
			siTask.setCenterX(mpp.x);
			siTask.setCenterY(mpp.y);
			int r = length(mpp, mrp);
			int power = jsl1.getValue();
			int vx = 0 < r ? power*Math.abs(mrp.x - mpp.x)/r : 0;
			int vy = 0 < r ? power*Math.abs(mrp.y - mpp.y)/r : 0;
			siTask.setRadius(r);
			siTask.setVector(vx, vy);
			siTask.setSpeed(jsl2.getValue());
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
