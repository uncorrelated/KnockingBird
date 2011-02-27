package com.uncorrelated;

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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.CMMException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class Knocking extends JFrame implements WindowListener, Runnable {
	private ImgCanvas canvas = null;
	private BufferedImage image = null;
	private int FrameRate = 10;
	private SwingBufferedImage[] swingBI = new SwingBufferedImage[10];
	private JSlider jsl1, jsl2, jsl3;
	private JPopupMenu pmenu;
	private JMenuItem[] jmi;
	private boolean IsDecline = false;
	private Thread thread = null;
	private volatile long waitOfThread;
	private volatile boolean flag = true;

	public Knocking() throws IOException {
		super("Knocking Bird");
		setUI();
		// http://www.flickr.com/photos/bikiniopen/3386409319/sizes/m/in/photostream/
		setImage(this.getClass().getResource("3386409319_7ca53351e8.jpg"));
		setResizable(false);
		addWindowListener(this);

		for (int c = 0; c < swingBI.length; c++)
			swingBI[c] = new SwingBufferedImage();

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.ipady = 8;
		gbc1.gridx = gbc1.gridy = 0;
		canvas = new ImgCanvas();
		canvas.setSize(image.getWidth(), image.getHeight());
		gbl.setConstraints(canvas, gbc1);
		add(canvas);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 0;
		gbc2.gridy = 1;
		Container jsls1 = new Container();
		jsls1.setLayout(new FlowLayout());
		Dimension dm = new Dimension(96, 16);
		JLabel jl1 = new JLabel("揺れの大きさ");
		jl1.setPreferredSize(dm);
		jsls1.add(jl1);
		jsls1.add(jsl1 = new JSlider(10, 50, 20));
		jsl1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int v = jsl1.getValue();
				synchronized(swingBI){
					for(int c=0;c<swingBI.length;c++){
						swingBI[c].changePower(v);
					}
				}
			}
		});
		gbl.setConstraints(jsls1, gbc2);
		add(jsls1);

		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.gridx = 0;
		gbc3.gridy = 2;
		Container jsls2 = new Container();
		jsls2.setLayout(new FlowLayout());
		JLabel jl2 = new JLabel("揺れの速さ");
		jl2.setPreferredSize(dm);
		jsls2.add(jl2);
		jsls2.add(jsl2 = new JSlider(1, 100, 25));
		jsl2.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int v = jsl2.getValue();
				synchronized(swingBI){
					for(int c=0;c<swingBI.length;c++){
						swingBI[c].setSpeed(v);
					}
				}
			}
		});
		gbl.setConstraints(jsls2, gbc3);
		add(jsls2);

		GridBagConstraints gbc4 = new GridBagConstraints();
		gbc4.gridx = 0;
		gbc4.gridy = 3;
		Container jsls3 = new Container();
		jsls3.setLayout(new FlowLayout());
		JLabel jl3 = new JLabel("湾曲部の丸み");
		jl3.setPreferredSize(dm);
		jsls3.add(jl3);
		jsls3.add(jsl3 = new JSlider(50, 150, 100));
		jsl3.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double v = getCoefficient();
				synchronized(swingBI){
					for(int c=0;c<swingBI.length;c++){
						swingBI[c].setCoefficient(v);
					}
				}
			}
		});
		gbl.setConstraints(jsls3, gbc4);
		add(jsls3);
		
		JCheckBox jcb4 = new JCheckBox("減退", IsDecline);
		jcb4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IsDecline = ((JCheckBox)e.getSource()).isSelected();
				synchronized(swingBI){
					for(int c=0;c<swingBI.length;c++){
						swingBI[c].setDecline(IsDecline);
					}
				}
				pmenu.setVisible(false);
			}
		});

		GridBagConstraints gbc6 = new GridBagConstraints();
		gbc6.gridx = 0;
		gbc6.gridy = 4;
		Container btns = new Container();
		btns.setLayout(new GridLayout(1, 4));
		btns.add(jcb4);
		JButton stop_b = new JButton("全停止");
		stop_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopAllSwing();
			}
		});
		btns.add(stop_b);
		gbl.setConstraints(btns, gbc6);
		add(btns);

		pmenu = new JPopupMenu();
		jmi = new JMenuItem[2];
		int jmi_c = 0;
		
		jmi[jmi_c] = new JMenuItem("マウス下の揺れを停止");
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.stopSwing();
			}
		});
		pmenu.add(jmi[jmi_c++]);

		jmi[jmi_c] = new JMenuItem("ファイルを選択");
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileChooser();
			}
		});
		pmenu.add(jmi[jmi_c++]);
		Dimension d_jmi = jmi[0].getPreferredSize();
		pmenu.setPopupSize(d_jmi.width, jmi.length*d_jmi.height);

		setSize();

		moveCenter();
		setVisible(true);

		setSize();
		moveCenter();

		waitOfThread = 1000/FrameRate;
		thread = new Thread(this);
		thread.start();
	}
	
	private void moveCenter(){
		Rectangle screen = getGraphicsConfiguration().getBounds();
		setLocation(screen.x + screen.width/2  - getSize().width/2,
				screen.y + screen.height/2 - getSize().height/2);
	}
	
	public double getCoefficient(){
		return ((double)200 - jsl3.getValue())/100;
	}
	
	private void stopAllSwing(){
		for (int c = 0; c < swingBI.length; c++)
			swingBI[c].reset();
	}
	
	public void run(){
		while (flag) {
			try {
				synchronized (swingBI) {
					for (int c = 0; c < swingBI.length; c++)
						swingBI[c].move();
					canvas.repaint();
				}
				synchronized (thread) {
					thread.wait(waitOfThread);
				}
			} catch (InterruptedException e) {
			}
		}
	}

	private void setSize() {
		if (null != image) {
			for (int c = 0; c < swingBI.length; c++)
				if(null!=swingBI[c])
					swingBI[c].reset();
			rescaleImage();
			if (null != canvas) {
				canvas.setSize(image.getWidth(), image.getHeight());
			}
			Dimension d = getPreferredSize();
			Insets is = getInsets();
			int width = d.width + is.left + is.right;
			int height = d.height + is.top + is.bottom;
			setSize(width, height);
			repaint();
		}
	}

	private void setImage(File file) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			image = ImageIO.read(fis);
			fis.close();
			setSize();
		} catch (CMMException e) {
			JOptionPane.showMessageDialog(this ,"このファイルのカラーコードには対応していません:\n" + e.getMessage(),"CMMException" ,JOptionPane.INFORMATION_MESSAGE);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this ,"ファイルが見つかりません:\n" + e.getMessage(),"FileNotFoundException" ,JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this ,"I/Oエラーです:\n" + e.getMessage(),"IOException" ,JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void setImage(URL url) throws IOException {
		image = ImageIO.read(url);
		setSize();
	}

	private void openFileChooser(){
		JFileChooser fc = new JFileChooser();
	    fc.setFileFilter(new FileFilter(){
			String[] exts = {".jpg", ".jpeg", ".png", ".gif"};
			public boolean accept(File f){
				for(int c=0;c<exts.length;c++){
					if(f.isDirectory())
						return true;
					if(f.getName().endsWith(exts[c]))
						return true;
				}
				return false;
			}
			public String getDescription() {
				return "JPG, PNG & GIF Images";
			}
		});
		int selected = fc.showOpenDialog(this);
		if (selected == JFileChooser.APPROVE_OPTION){
			setImage(fc.getSelectedFile());
		}
	}

	public boolean rescaleImage() {
		int original_height = image.getHeight();
		int destination_height = original_height;
		int original_width = image.getWidth();
		int destination_width = original_width;
		int maximum_size = 480;
		boolean IsLarge = false;
		if (maximum_size < original_width) {
			destination_width = maximum_size;
			destination_height = destination_width * original_height
					/ original_width;
			IsLarge = true;
		}
		if (maximum_size < original_height) {
			destination_height = maximum_size;
			destination_width = destination_height * original_width
					/ original_height;
			IsLarge = true;
		}
		if (IsLarge) {
			float scaling = ((float) (destination_width + 1))
					/ ((float) original_width);
			HashMap hm = new HashMap();
			hm.put(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			hm.put(RenderingHints.KEY_DITHERING,
					RenderingHints.VALUE_DITHER_ENABLE);
			BufferedImage image2 = new BufferedImage(destination_width,
					destination_height, image.getType());
			AffineTransformOp atfp = new AffineTransformOp(
					AffineTransform.getScaleInstance(scaling, scaling),
					new RenderingHints(hm));
			atfp.filter(image, image2);
			image = image2;
		}
		return IsLarge;
	}

	private class ImgCanvas extends Canvas implements MouseListener,
			MouseMotionListener, DropTargetListener {
		private Point mpp = null, mrp = null, mmp = null;
		private int cptr = 0;
		private boolean IsMeasureFrameRate = false;
		private int nof = 0, sec = (int)System.currentTimeMillis()/1000;
		private int mouse_x, mouse_y;

		public ImgCanvas() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}

	
		public void paint(Graphics g) {
			Image dbuf = createImage(image.getWidth(), image.getHeight());
			Graphics gd = dbuf.getGraphics();
			BufferedImage bi = image;
			for (int c = 0; c < swingBI.length; c++)
				bi = swingBI[c].transform(bi);
			gd.drawImage(bi, 0, 0, this);
			if (null != mpp && null != mmp) {
				int radius = length(mpp, mmp);
				drawBoldCircle(gd, mpp, radius, Color.yellow);
				gd.setColor(Color.black);
				gd.drawLine(mpp.x + 1, mpp.y, mmp.x + 1, mmp.y);
				gd.drawLine(mpp.x - 1, mpp.y, mmp.x - 1, mmp.y);
				gd.setColor(Color.yellow);
				gd.drawLine(mpp.x, mpp.y, mmp.x, mmp.y);
			}
			drawSwingCircle(gd);
			g.drawImage(dbuf, 0, 0, this);
			if(IsMeasureFrameRate){
				nof++;
				int csec = (int)(System.currentTimeMillis()/1000);
				if(sec < csec){
					int mt = csec - sec;
					System.out.println((float)nof/mt);
					sec = csec;
					nof = 0;
				}
			}
		}

		private int length(Point p1, Point p2) {
			int dx = p1.x - p2.x;
			int dy = p1.y - p2.y;
			return (int) Math.sqrt(dx * dx + dy * dy);
		}

		private void drawCircle(Graphics g, int x, int y, int r) {
			g.drawArc(x - r, y - r, 2 * r, 2 * r, 0, 360);
		}

		private void drawBoldCircle(Graphics g, Point p, int r, Color color){
			g.setColor(Color.black);
			drawCircle(g, p.x, p.y, r - 1);
			drawCircle(g, p.x, p.y, r + 1);
			g.setColor(color);
			drawCircle(g, p.x, p.y, r);
		}

		public void stopSwing(){
			for (int c = 0; c < swingBI.length; c++){
				int dx = swingBI[c].getCenterX() - mouse_x;
				int dy = swingBI[c].getCenterY() - mouse_y;
				int r = swingBI[c].getRadius();
				if(r*r >= dx*dx + dy*dy){
					swingBI[c].reset();
				}
			}
		}
		
		public void drawSwingCircle(Graphics g){
			for (int c = 0; c < swingBI.length; c++){
				int cx = swingBI[c].getCenterX();
				int dx = cx - mouse_x;
				int cy = swingBI[c].getCenterY();
				int dy = cy - mouse_y;
				int r = swingBI[c].getRadius();
				if(r*r >= dx*dx + dy*dy){
					drawBoldCircle(g, new Point(cx, cy), r, Color.pink);
				}
			}
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
			if (MouseEvent.BUTTON1 == arg0.getButton()) {
				mpp = arg0.getPoint();
			} else if (arg0.isPopupTrigger()) {
				pmenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			}
		}

		public void mouseReleased(MouseEvent arg0) {
			if (MouseEvent.BUTTON1 == arg0.getButton()) {
				mrp = arg0.getPoint();
				synchronized(swingBI){
					cptr = cptr % swingBI.length;
					swingBI[cptr].setCenterX(mpp.x);
					swingBI[cptr].setCenterY(mpp.y);
					int r = length(mpp, mrp);
					int power = jsl1.getValue();
					int vx = 0 < r ? power * (mrp.x - mpp.x) / r : 0;
					int vy = 0 < r ? power * (mrp.y - mpp.y) / r : 0;
					swingBI[cptr].setRadius(r);
					swingBI[cptr].setVector(vx, vy);
					swingBI[cptr].setSpeed(jsl2.getValue());
					swingBI[cptr].setDecline(IsDecline);
					swingBI[cptr].setPower(power);
					swingBI[cptr].setCoefficient(getCoefficient());
					mrp = mpp = mmp = null;
					cptr = (cptr + 1) % swingBI.length;
				}
			} else if (arg0.isPopupTrigger()) {
				pmenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			}
		}

		public void mouseDragged(MouseEvent e) {
			mmp = e.getPoint();
		}

		public void mouseMoved(MouseEvent e) {
			mouse_x = e.getX();
			mouse_y = e.getY();
		}

		private DropTarget dropTarget = new DropTarget(this,
				DnDConstants.ACTION_COPY, this, true);

		public void dragEnter(DropTargetDragEvent arg0) {
			arg0.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		}

		public void dragExit(DropTargetEvent arg0) {
		}

		public void dragOver(DropTargetDragEvent arg0) {
			if (arg0.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				arg0.acceptDrag(DnDConstants.ACTION_COPY);
				return;
			} else if (arg0.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				arg0.acceptDrag(DnDConstants.ACTION_COPY);
				return;
			}
			arg0.rejectDrag();
		}

		public void drop(DropTargetDropEvent arg0) {
			try {
				if (arg0.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					arg0.acceptDrop(DnDConstants.ACTION_COPY);
					Transferable trans = arg0.getTransferable();
					java.util.List files = (java.util.List) trans
							.getTransferData(DataFlavor.javaFileListFlavor);
					Iterator it = files.iterator();
					if (it.hasNext()) {
						setImage((File) it.next());
					}
				} else if (arg0.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					if (arg0.isLocalTransfer()) {

					} else {
						arg0.acceptDrop(DnDConstants.ACTION_COPY);
						Transferable trans = arg0.getTransferable();
						String fnames = (String) trans
								.getTransferData(DataFlavor.stringFlavor);
						StringTokenizer st = new StringTokenizer(fnames, "\n");
						if(st.hasMoreTokens()){
							setImage(new URL(st.nextToken()));
						}
					}
				}
				arg0.dropComplete(true);
			} catch (UnsupportedFlavorException ex) {
				ex.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				arg0.dropComplete(false);
			}
		}

		public void dropActionChanged(DropTargetDragEvent arg0) {
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
		Knocking bubble = new Knocking();
	}
}
