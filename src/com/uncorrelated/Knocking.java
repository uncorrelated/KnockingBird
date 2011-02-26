package com.uncorrelated;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
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
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

public class Knocking extends JFrame implements WindowListener {
	private Canvas canvas = null;
	private BufferedImage image = null;
	private Timer timer = null;
	private SwingBufferedImage[] swingBI = new SwingBufferedImage[2];
	private JSlider jsl1, jsl2;
	private JPopupMenu pmenu;
	private JMenuItem[] jmi;

	public Knocking() throws IOException {
		super("Image Swinger");
		setImage(this.getClass().getResource("natsume_nana.jpg"));
		setResizable(false);
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
		for (int c = 0; c < swingBI.length; c++)
			swingBI[c] = new SwingBufferedImage();

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
		jsl1 = new JSlider(5, 40, 20);
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
		jsl2 = new JSlider(2, 10, 5);
		gbl.setConstraints(jsl2, gbc5);
		add(jsl2);

		pmenu = new JPopupMenu();
		jmi = new JMenuItem[5];
		pmenu.setPopupSize(112, 96);
		jmi[0] = new JMenuItem();
		jmi[0].setLayout(new GridLayout(1, 1));
		JLabel jl_stop = new JLabel("停止");
		jmi[0].add(jl_stop);
		jmi[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int c = 0; c < swingBI.length; c++)
					swingBI[c].reset();
			}
		});
		pmenu.add(jmi[0]);

		ButtonGroup group = new ButtonGroup();
		JRadioButton jcb1 = new JRadioButton("1箇所揺らす", false);
		JRadioButton jcb2 = new JRadioButton("2箇所揺らす", true);
		JRadioButton jcb3 = new JRadioButton("3箇所揺らす", true);
		jcb1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeSBNumber(1);
				pmenu.setVisible(false);
			}
		});
		jcb2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeSBNumber(2);
				pmenu.setVisible(false);
			}
		});
		jcb3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeSBNumber(3);
				pmenu.setVisible(false);
			}
		});
		group.add(jcb1);
		group.add(jcb2);
		group.add(jcb3);

		jmi[1] = new JMenuItem();
		jmi[1].setLayout(new GridLayout(1, 1));
		jmi[1].add(jcb1);
		pmenu.add(jmi[1]);

		jmi[2] = new JMenuItem();
		jmi[2].setLayout(new GridLayout(1, 1));
		jmi[2].add(jcb2);
		pmenu.add(jmi[2]);

		jmi[3] = new JMenuItem();
		jmi[3].setLayout(new GridLayout(1, 1));
		jmi[3].add(jcb3);
		pmenu.add(jmi[3]);

		jmi[4] = new JMenuItem();
		jmi[4].setLayout(new GridLayout(1, 1));
		jmi[4].add(new JLabel("ファイルを選択"));
		jmi[4].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileChooser();
			}
		});
		pmenu.add(jmi[4]);
		
		setUI();
		setVisible(true);
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				synchronized (timer) {
					for (int c = 0; c < swingBI.length; c++)
						swingBI[c].move();
				}
				canvas.repaint();
			}
		}, 0, 50);
	}
	
	private void changeSBNumber(int n) {
		synchronized (timer) {
			jmi[n].setSelected(true);
			swingBI = new SwingBufferedImage[n];
			for (int c = 0; c < swingBI.length; c++)
				swingBI[c] = new SwingBufferedImage();
		}
	}

	private void setSize() {
		if (null != image) {
			for (int c = 0; c < swingBI.length; c++)
				if(null!=swingBI[c])
					swingBI[c].reset();
			rescaleImage();
			setSize(image.getWidth() + 64, image.getHeight() + 128);
			if (null != canvas) {
				canvas.setSize(image.getWidth(), image.getHeight());
			}
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

		private int length(Point p1, Point p2) {
			int dx = p1.x - p2.x;
			int dy = p1.y - p2.y;
			return (int) Math.sqrt(dx * dx + dy * dy);
		}

		private void drawCircle(Graphics g, int x, int y, int r) {
			g.drawArc(x - r, y - r, 2 * r, 2 * r, 0, 360);
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
				if (cptr >= swingBI.length)
					cptr = 0;
				swingBI[cptr].setCenterX(mpp.x);
				swingBI[cptr].setCenterY(mpp.y);
				int r = length(mpp, mrp);
				int power = jsl1.getValue();
				int vx = 0 < r ? power * Math.abs(mrp.x - mpp.x) / r : 0;
				int vy = 0 < r ? power * Math.abs(mrp.y - mpp.y) / r : 0;
				swingBI[cptr].setRadius(r);
				swingBI[cptr].setVector(vx, vy);
				swingBI[cptr].setSpeed(jsl2.getValue());
				mrp = mpp = mmp = null;
				cptr = (cptr + 1) % swingBI.length;
			} else if (arg0.isPopupTrigger()) {
				pmenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			}
		}

		public void mouseDragged(MouseEvent e) {
			mmp = e.getPoint();
		}

		public void mouseMoved(MouseEvent e) {
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
