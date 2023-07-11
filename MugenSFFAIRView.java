/*
Copyright (C) 2023 kumohakase (license: https://creativecommons.org/licenses/by-nc/4.0/)
MUGEN SFF AIR Viewer
Please consider supporting me using kofi.com https://ko-fi.com/kumohakase
*/

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;

public class MugenSFFAIRView extends JFrame
				implements ActionListener, ComponentListener, ChangeListener {
	//Screen image buffer
	BufferedImage Scr;
	Graphics2D G;
	//Group Number, Image Number, ImageIndex, AnimationNumber TextField
	JTextField TGroupNum, TImageNum, TImageIndex, TAnimNumber; 
	JSlider SAnimNo, SImgIndex; //Animation No, Image Index Selector
	PictureBox PicBox; //JPanel to show screen image buffer
	SFFElement SFFImgArray[] = null; //Sprite data array from sff
	int SelectedImageIndex = -1; //Image index to show, -1 if not selected
	AIRElement AIRAnimationDescs[] = null; //Array of animation descriptor array
	int SelectedAnimIndex = -1; //Selected animation index
	int CurrentFrame; //Current frame when play mode
	int FrameTimer; //For measuring display time
	Timer TMAnim; //Animation timer
	public static void main(String[] args) {
		new MugenSFFAIRView();
	}
	public MugenSFFAIRView() {
		super("Mugen SFF AIR Viewer");
		TMAnim = new Timer(15, this);
		TMAnim.setActionCommand("animate");
		//Animation timer called every 15mS
		Timer t1 = new Timer(15, this);
		t1.setActionCommand("draw");
		add( MakeUI() ); //Make UI and add
		InitCanvas(640, 480);//Initialize screen buffer
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setMinimumSize( new Dimension(640, 480) );
		setVisible(true);
		t1.start(); //Start drawing animation
	}
	
	//Make UI
	public JPanel MakeUI() {
		JPanel p = new JPanel( new GridLayout(3, 1) ); //3 stack panel
		//Stack 1: file operations
		JPanel p1 = new JPanel( new GridLayout(1, 3) );
		//Add buttons: Load SFF, Load AIR, Export to PNG
		for(int i = 0; i < 3; i++) {
			final String L[] = {"Load SFF", "Load AIR", "Export to PNG"};
			final String C[] = {"sff", "air", "export"};
			JButton b = new JButton(L[i]);
			b.setActionCommand(C[i]);
			b.addActionListener(this);
			p1.add(b);
		}
		p.add(p1);
		//Stack2: Image finder
		JPanel p2 = new JPanel( new GridBagLayout() );
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridy = 0;
		//Add textareas: Group#, Image#, Index
		TGroupNum = new JTextField(5);
		TImageNum = new JTextField(5);
		TImageIndex = new JTextField(5);
		for(int i = 0; i < 3; i++) {
			final String L[] = {"Group#", "Image#", "Index"};
			final JTextField C[] = {TGroupNum, TImageNum, TImageIndex};
			gbc2.gridx = i * 2;
			p2.add( new JLabel(L[i]), gbc2 );
			gbc2.gridx = i * 2 + 1;
			p2.add(C[i], gbc2);
		}
		//Add find button
		JButton bFind = new JButton("Find");
		bFind.setActionCommand("find");
		bFind.addActionListener(this);
		gbc2.gridx = 6;
		p2.add(bFind, gbc2);
		//Add image index slider
		SImgIndex = new JSlider();
		SImgIndex.addChangeListener(this);
		gbc2.gridx = 7;
		gbc2.weightx = 1.0;
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		p2.add(SImgIndex, gbc2);
		p.add(p2);
		//Stack3: Animation control
		JPanel p3 = new JPanel( new GridBagLayout() );
		GridBagConstraints gbc3 = new GridBagConstraints();
		//Add Animation Index Selector Textbox
		//Add Label
		gbc3.gridx = 0;
		gbc3.gridy = 0;
		p3.add( new JLabel("Animation Number"), gbc3);
		//Add Textbox
		gbc3.gridx = 1;
		TAnimNumber = new JTextField(5);
		p3.add(TAnimNumber, gbc3);
		//Add play button
		JButton bPlay = new JButton("Play");
		bPlay.setActionCommand("play");
		bPlay.addActionListener(this);
		gbc3.gridx = 2;
		p3.add(bPlay, gbc3);
		p.add(p3);
		//Add slide bar
		SAnimNo = new JSlider();
		gbc3.gridx = 3;
		gbc3.weightx = 1.0;
		gbc3.fill = GridBagConstraints.HORIZONTAL;
		p3.add(SAnimNo, gbc3);
		//Return panel: top: PictureBox, botton: ControlPanel
		PicBox = new PictureBox();
		PicBox.addComponentListener(this);
		JPanel r = new JPanel( new BorderLayout() );
		r.add(PicBox,  BorderLayout.CENTER);
		r.add(p, BorderLayout.SOUTH);
		return r;
	}
	
	//Button press or timer tick primary callback
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String cmd = e.getActionCommand();
		if(cmd.equals("sff")) {
			opensff();
		} else if(cmd.equals("air")) {
			openair();
		} else if(cmd.equals("export")) {
			export();
		} else if(cmd.equals("find")) {
			findimg();
		} else if(cmd.equals("play")) {
			playair();
		} else if(cmd.equals("draw")) {
			draw();
		} else if(cmd.equals("animate")) {
			animate();
		}
	}
	
	/**
	 * Open file selection dialogue, return selected file if exists.
	 * @param filedesc File description
	 * @param ext File extension without dot
	 * @return Selected file object if OK clicked, otherwise null
	 */
	public File OpenFileDialogue(String filedesc, String ext) {
		JFileChooser c = new JFileChooser();
		FileNameExtensionFilter f = 
				new FileNameExtensionFilter(filedesc, ext);
		c.setFileFilter(f);
		c.setDialogTitle("Mugen SFF AIR Viewer");
		//Return selected file if OK clicked and file exists.
		if(c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return  c.getSelectedFile();
		}
		return null;
	}
	
	/**
	 * Show information popup dialogue
	 * @param content Message
	 */
	public void InformationDialogue(String content) {
		JOptionPane.showMessageDialog(this, content, 
				"Mugen SFF AIR Viewer", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Show error popup dialogue
	 * @param content Message
	 */
	public void ErrorDialogue(String content) {
		JOptionPane.showMessageDialog(this, content, 
				"Mugen SFF AIR Viewer", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * convert byte array to unsigned integer
	 * @param d byte array
	 * @param o offset
	 * @param s length
	 * @return little endian unsigned integer of d (index o to s)
	 */
	public long btoui(byte[] d, int o, int s) {
		long r = 0;
		for(int i = 0; i < s; i++) {
			r += Byte.toUnsignedInt(d[i + o]) << (i * 8);
		}
		return r;
	}
	
	/**
	 * convert byte array to signed integer
	 * @param d byte array
	 * @param o offset
	 * @param s length, 1, 2 or 4
	 * @return little endian unsigned integer of d (index o to s)
	 */
	public int btoi(byte[] d, int o, int s) {
		long t = btoui(d, o, s);
		if(s == 1) {
			if(t > 127) {
				return (int)t - 256;
			}
		} else if(s == 2) {
			if(t > 32767) {
				return (int)t - 65536;
			}
		} else if(s == 4) {
			if(t > 2147483647L) {
				return (int)(t - 4294967296L);
			}
		}
		return (int)t;
	}
	
	/**
	 * Initializes screen buffer
	 * @param w width
	 * @param h height
	 */
	public void InitCanvas(int w, int h) {
		Scr = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		PicBox.ChangeImg(Scr);
		G = Scr.createGraphics();
		G.setBackground(Color.BLACK);
		G.setColor(Color.white);
		G.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	/**
	 * Get BufferedImage from SFFImgArray[ind]
	 * @param ind Image index to get
	 * @param transp treat colour index=0 as transparent like mugen if true
	 */
	public BufferedImage ObtainBFImage(int ind, boolean transp) {
		SFFElement e = SFFImgArray[ind];
		byte img[];
		Color[] p = e.Palette;
		img = e.ImgContent;
		int imagew = e.ImgWidth;
		int imageh = e.ImgHeight;
		//If shared palette or there was no palette , use img[0]'s pal
		if(e.SharedPalette || p == null) {
			p = SFFImgArray[0].Palette;
		}
		//if img is null, it is linked image
		if(img == null && e.LinkId < SFFImgArray.length &&
				SFFImgArray[e.LinkId].ImgContent != null) {
			img = SFFImgArray[e.LinkId].ImgContent;
			imagew = SFFImgArray[e.LinkId].ImgWidth;
			imageh = SFFImgArray[e.LinkId].ImgHeight;
		}
		if(img != null) {
			//Convert indexed colour image byte[] to BufferedImage
			BufferedImage _img = new BufferedImage(imagew, imageh, 
					BufferedImage.TYPE_INT_ARGB);
			int bi = 0;
			for(int ey = 0; ey < imageh; ey++) {
				for(int ex = 0; ex < imagew; ex++) {
					int px = Byte.toUnsignedInt(img[bi]);
					bi++;
					//Skip painting if transp flag is set and colour index = 0
					if(px == 0 && transp) { continue; } 
					int r = p[px].getRed();
					int g = p[px].getGreen();
					int b = p[px].getBlue();
					_img.setRGB(ex, ey, 0xff000000 + (r << 16) + (g << 8) + b);
				}
			}
			return _img;
		} else {
			return null;
		}
	}
	
	/**
	 * decode pcx bytearray and convert to BufferedImage
	 * @param img pcx bytearray
	 * @param i index to store image
	 * @throws IllegalArgumentException
	 * if pcx is weird or not suitable for MUGEN
	 */
	public void decodepcx(byte[] img, int ind)
			throws IllegalArgumentException {
		Color _pal[] = new Color[256];
		//Check length, error if img is shorter than header size
		if(img.length < 128) {
			throw new IllegalArgumentException("PCX is too short!");
		}
		//Grab some important info
		int filesig = btoi(img, 0, 1); //0x0 uint8_t signature (const: 10)
		int filever = btoi(img, 1, 1); //0x1 uint8_t filever
		//0x2 uint8_t compresds method (usually 1 (RLE))
		int compmethod = btoi(img, 2, 1);
		int nbplane = btoi(img, 3, 1); //0x3 uint8_t Image plane bit count
		//Image size parameters
		int minx = (int)btoui(img, 4, 2); //0x4 uint16_t Min X
		int miny = (int)btoui(img, 6, 2); //0x6 uint16_t Min Y
		int maxx = (int)btoui(img, 8, 2); //0x8 uint16_t Max X
		int maxy = (int)btoui(img, 10, 2); //0xa uint16_t Min Y
		int imgwidth = maxx - minx + 1;
		int imgheight = maxy - miny + 1;
		SFFImgArray[ind].ImgWidth = imgwidth;
		SFFImgArray[ind].ImgHeight = imgheight;
		int nplane = btoi(img, 65, 1); //0x41 uint8_t Image plane count
		int nscanline = (int)btoui(img, 66, 2); //0x42 uint16_t scanline size
		//Check pcx file identifier
		if(filesig != 10) {
			throw new IllegalArgumentException("PCX file identifier is wrong!");
		}
		//Check if the pcx is suitable for mugen
		//plane bit = 8, plane count = 1, filever = 5
		if(!(nbplane == 8 && nplane == 1 && filever == 5)) {
			throw new IllegalArgumentException("PCX not suitable for MUGEN");
		}
		//Decode palette data if desired.
		if(SFFImgArray[ind].SharedPalette == false || ind == 0) {
			int paloff = img.length - 769; //palette data offset
			if(paloff < 0) {
				throw new IllegalArgumentException(
						"Individual palette but filelen "
						+ "too short to have palette data");
			}
			//If start of palette is not 12, error
			if(btoui(img, paloff, 1) != 12) {
				throw new IllegalArgumentException("PCX palette is missing.");
			}
			for(int i = 0; i < 256; i++) {
				//palette data is uint8_t rgb tuple
				int _r = (int)btoui(img, paloff + (i * 3) + 1, 1);
				int _g = (int)btoui(img, paloff + (i * 3) + 2, 1);
				int _b = (int)btoui(img, paloff + (i * 3) + 3, 1);
				_pal[i] = new Color(_r, _g, _b);
			}
			SFFImgArray[ind].Palette = _pal;
			
		}
		int byteptr = 0; //raw byte pointer
		int ubyteptr = 0; //uncompressed byte pointer
		int imgptr = 0; //added pixel count
		int rl = -1; //runlength, -1 if there is no runlength bit
		SFFImgArray[ind].ImgContent = new byte[imgwidth * imgheight];
		while(byteptr + 128 < img.length && imgptr < imgwidth * imgheight) {
			//read single byte from 128 octet
			int e = (int)btoui(img, byteptr + 128, 1);
			if(e < 0xc0 || rl != -1 || compmethod == 0) {
				//0x0 - 0xbf or previous bit is runlength 
				if(rl == -1) {rl = 1;}
				//add pixel for rl times
				for(int i = 0; i < rl; i++) {
					int ix = ubyteptr % nscanline;
					if(ix < imgwidth) {
						byte t;
						//convert int16_t to int8_t
						if(e < 128) {
							t = (byte)e;
						} else {
							t = (byte)(e - 256);
						}
						SFFImgArray[ind].ImgContent[imgptr] = t;
						imgptr++;
					}
					ubyteptr++;
				}
				rl = -1;
			} else {
				rl = e - 0xc0; //0xc0 - 0xff: runlength
			}
			byteptr += 1;
		}
	}
	
	//Open SFF button pressed callback
	public void opensff() {
		File f = OpenFileDialogue("SFFv1 MUGEN imagearray", "sff");
		if(f == null) { return; } //Exit when unselected
		stopAnimation();
		try(RandomAccessFile sff = new RandomAccessFile(f, "r")) {
			//Read file header
			byte h[] = new byte[32];
			sff.readFully(h);
			//Check for identifier (first 16 bytes of file)
			//Correct identifier: ElecbyteSpr\0\0\x01\0\x01
			final byte IDENT[] = 
				{'E', 'l', 'e', 'c', 'b', 'y', 't', 'e', 'S', 'p', 'r',
				0, 0, 1, 0, 1};
			for(int i = 0; i < 16; i++) {
				if(IDENT[i] != h[i]) {
					ErrorDialogue("This is not sff file.");
					return;
				}
			}
			int imgcount = btoi(h, 0x14, 4); //0x14 int32_t imagecount
			//0x18 int32_t first file pointer
			int shptr = btoi(h, 0x18, 4);
			//image count must be in 0-65535, shptr must be unsigned
			if(!(0 <= imgcount && imgcount <= 65535 && 0 <= shptr)) {
				ErrorDialogue("Header parameter is weird!");
				return;
			}
			SFFImgArray = new SFFElement[imgcount]; //initialize imgarray
			//extract each image
			for(int i = 0; i < imgcount; i++) {
				//read next subheader
				byte sh[] = new byte[0x13];
				sff.seek(shptr);
				sff.readFully(sh);
				int nextaddr = btoi(sh, 0, 4); //0x0 int32_t nextptr
				int imglen = btoi(sh, 4, 4); //0x4 int32_t image length
				int px = btoi(sh, 8, 2); //0x8 int16_t axis x
				int py = btoi(sh, 10, 2); //0xa int16_t axis y
				int pgrp = (int)btoui(sh, 12, 2); //0xc uint16_t group#
				int pimgn = (int)btoui(sh, 14, 2); //0xe uint16_t image#
				int plin = (int)btoui(sh, 16, 2); //0x10 uint16_t linknum
				boolean pshared = false;
				if(sh[18] == 1) { pshared = true; } //0x12 uint8_t shared
				System.out.printf("SFF load: %d: Group%d Image%d %dx%d\n",
									i, pgrp, pimgn, px, py);
				//Add SFF element into array
				SFFImgArray[i] =
						new SFFElement(px, py, pgrp, pimgn, pshared, plin);
				byte imgdata[] = new byte[imglen];
				//Actual data is stored if imglen != 0
				if(imglen != 0) {
					sff.seek(shptr + 0x20); //seek to image
					sff.readFully(imgdata); //read img
					decodepcx(imgdata, i); //Store image to array
				}
				shptr = nextaddr; //update next file pointer
			}
			SImgIndex.setMaximum(imgcount - 1);
			InformationDialogue(String.format("%d images loaded.", imgcount));
		} catch(IOException ex) {
			System.out.println(ex.getMessage());
			SFFImgArray = null; //reset imgarray
			ErrorDialogue("File read failure.");
		} catch(IllegalArgumentException ex) {
			//When new SFFElement() or updateimg() failed
			System.out.println(ex.getMessage());
			SFFImgArray = null;
			ErrorDialogue("SFF subfile is weird!");
		}
	}
	
	/**
	 * Add animation descriptor to AIRAnimationDescs
	 * @param e data
	 */
	public void AppendAnimation(AIRElement e) throws IllegalArgumentException {
		//Check if there is illegal Loopstart
		if(e.LoopstartNum >= e.Elements.length) {
			throw new IllegalArgumentException("Bad loopstart!");
		}
		//Expand and append element to array
		AIRElement n[] = new AIRElement[AIRAnimationDescs.length + 1];
		System.arraycopy(AIRAnimationDescs, 0, n, 0, AIRAnimationDescs.length);
		n[n.length - 1] = e;
		AIRAnimationDescs = n;
	}
	
	//Open AIR button pressed callback
	public void openair() {
		//Load AIR file
		//Ask for air file, return if unselected
		File f = OpenFileDialogue("MUGEN animation script", "air");
		if(f == null) {return;}
		stopAnimation();
		//Read all lines of text file
		int lineno = 0;
		String line = "";
		AIRElement anim = null;
		AIRAnimationDescs = new AIRElement[0];
		try(BufferedReader br = new BufferedReader(new FileReader(f) ) ) {
			line = br.readLine();
			while(line != null) {
				lineno++;
				//Ignore comment (after ;)
				int s = line.indexOf(';');
				if(s != -1) {
					line = line.substring(0, s);
				}
				line = line.trim().toLowerCase();
				//if line is empty after ignoring comment, read next line
				if(line.equals("")) {
					line = br.readLine();
					continue;
				}
				//Process air line
				if(line.startsWith("[begin action ")) {
					/*
					 [Begin Action nnn] n: number will define animation number
					 this line should be appeared before defining animation
					 elements
					 */
					int e = line.indexOf(']');
					if(e != -1) {
						String t = line.substring(14, e);
						//Add collected animation element before proceeding
						//new animation definition
						if(anim != null) {
							AppendAnimation(anim);
						}
						anim = new AIRElement(Integer.parseInt(t), 0);
					} else {
						throw new IllegalArgumentException("Excepted ]");
					}
				} else if(line.equals("loopstart")) {
					/*
					 Loopstart statement will define animation's loop start
					 point from 2nd loop.
					 */
					if(anim != null) {
						anim.LoopstartNum = anim.Elements.length;
					} else {
						final String desc = "Animation number undefined.";
						throw new IllegalArgumentException(desc);
					}
				} else if(line.startsWith("clsn1") || 
						line.startsWith("clsn2") || 
						line.startsWith("clsn1Default") || 
						line.startsWith("clsn2Default")) {
					/*
					 lines start with Clsn1, Clsn2, Clsn1Default, Clsn2Default
					 is for definition of hitboxes. ignore this for now.
					 */
				} else {
					/*
					 Lines that don't meet any of those conditions, should be
					 animation element. Format: g,i, x,y, l
					 where g: group number, i: image number,
					 x,y : sprite offset coordinate, l: display time
					 (60 means 1sec), specifying -1 in final animation element
					 will prevent this animation from looping.
					 */
					if(anim == null) {
						final String desc = "Animation number undefined.";
						throw new IllegalArgumentException(desc);
					}
					//Append
					AIRElementSub e = new AIRElementSub(line);
					anim.AppendAnimationElement(e);
				}
				line = br.readLine();
			}
			//Add last collected animation element
			if(anim != null) {
				AppendAnimation(anim);
			}
			SAnimNo.setMaximum(AIRAnimationDescs.length - 1);
			InformationDialogue(String.format("%d animations loaded.", 
					AIRAnimationDescs.length));
		} catch(IOException ex) {
			AIRAnimationDescs = null;
			System.out.println(ex.getMessage());
			ErrorDialogue("AIR file reading failed");
		} catch(NumberFormatException ex) {
			AIRAnimationDescs = null;
			ErrorDialogue(String.format("Parse failed\n%s\nat line %d\n" +
						"Integer conversion failed." ,line ,lineno) );
			return;
		} catch (IllegalArgumentException ex) {
			AIRAnimationDescs = null;
			ErrorDialogue(String.format("Parse failed\n%s\nat line %d\n%s"
							,line ,lineno, ex.getMessage()) );
			return;
		}
	}
	
	//Export to PNG pressed callback
	public void export() {
		//If SFF is not loaded, exit
		if(SFFImgArray == null) {
			ErrorDialogue("Load SFF first.");
			return;
		}
		//Ask save directory
		JFileChooser dirc = new JFileChooser();
		dirc.setDialogTitle("Mugen SFF AIR Viewer");
		dirc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(dirc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File savedir = dirc.getSelectedFile();
		//If file/dir already exists, abort
		if(savedir.exists()) {
			ErrorDialogue("Already exists!");
			return;
		}
		savedir.mkdir();
		File frep = new File(savedir, "report.html"); //report file
		try(BufferedWriter rep = new BufferedWriter(new FileWriter(frep))) {
			//Write report file header
			rep.write("<!DOCTYPE html><html><head><meta charset=\"utf-8\">" +
					"</head><body>SFF export report from MugenSFFAIRview<br>" +
					"by Kumohakase, please consider support me via " +
					"<a href=\"https://ko-fi.com/kumohakase\">kofi</a>.");
			rep.write(String.format("<h1>Total %d images</h1>",
					SFFImgArray.length));
			rep.write("<table border=1><tr><th>#</th><th>Group#</th>" +
						"<th>Image#</th><th>X</th><th>Y</th>" +
						"<th>Palette Type</th><th>Linked?</th><th>Image</th>" +
						"</tr>");
			//Export all images in SFFImageArray
			for(int i = 0; i < SFFImgArray.length; i++) {
				SFFElement e = SFFImgArray[i];
				File f = new File(savedir, String.format("%d.png",i));
				int aid = i;
				if(e.ImgContent != null) {
					//Write as png under savedir as i.png
					BufferedImage img = ObtainBFImage(i, false);
					ImageIO.write(img, "png", f);
				}
				//Add report
				String s = "Individual";
				if(e.SharedPalette) { s = "Shared"; }
				String l = "Actual";
				if(e.ImgContent == null) {
					l = String.format("Linked to %d", e.LinkId);
					aid = e.LinkId;
				}
				rep.write(String.format("<tr><td><b>%d</b></td><td>%d</td>" + 
						"<td>%d</td><td>%d</td><td>%d</td><td>%s</td>" + 
						"<td>%s</td><td><img src=\"%d.png\"></td></tr>",
						i, e.GroupNo, e.ImageNo, e.X, e.Y, s, l, aid) );
			}
			//Write report footer
			rep.write("</table></body></html>");
		} catch(IOException ex) {
			ErrorDialogue(ex.getMessage());
			return;
		}
		InformationDialogue("Exported");
	}
	
	//Find button pressed callback
	public void findimg() {
		//If SFF is not loaded, exit
		if(SFFImgArray == null) {
			ErrorDialogue("Load SFF first.");
			return;
		}
		String _gn = TGroupNum.getText();
		String _in = TImageNum.getText();
		String _iind = TImageIndex.getText();
		//Error when neither GroupNo, ImageNo or Index specified.
		if(_gn.equals("") && _in.equals("") && _iind.equals("")) {
			ErrorDialogue(
					"Please specify at least index or groupno and imageno");
			return;
		}
		//Error when groupno or imageno specified when index is specified
		if(!_iind.equals("") && (!_gn.equals("") || !_in.equals(""))) {
			ErrorDialogue(
					"Can not specify index and other info at the same time");
			return;
		}
		//If image index was specified
		if(!_iind.equals("")) {
			try {
				int i = Integer.parseInt(_iind);
				if(0 <= i && i < SFFImgArray.length) {
					stopAnimation();
					SelectedImageIndex = i;
				} else {
					ErrorDialogue("Out of range");
				}
			} catch (NumberFormatException ex) {
				ErrorDialogue("Bad index format.");
			}
			return;
		}
		//Get specified group number and image number and try to convert to int
		int FGrp, FImg;
		try {
			FGrp = Integer.parseInt(_gn);
			FImg = Integer.parseInt(_in);
		} catch(NumberFormatException ex) {
			ErrorDialogue("Please input valid group number and image number");
			return;
		}
		int r = _findimg(FGrp, FImg);
		if(r == -1) {
			InformationDialogue("Not found.");
		} else {
			stopAnimation();
			SelectedImageIndex = r;
		}
	}
	
	/**
	 * Find image that have specified groupno and imageno and return index
	 * if not found, returns -1
	 */
	public int _findimg(int groupno, int imageno) {
		for(int i = 0; i < SFFImgArray.length; i++) {
			SFFElement e = SFFImgArray[i];
			if(groupno == e.GroupNo && imageno == e.ImageNo) {
				return i;
			}
		}
		return -1;
	}
	
	//Play button pressed callback
	public void playair() {
		if(AIRAnimationDescs == null) {
			ErrorDialogue("Please loas AIR first");
			return;
		}
		//Find animation descriptor that has shares same animation number
		//with TAnimNumber's input
		//if TAnimNumber is empty, index is specified directry by SAnimIndex
		//slider
		int fe = -1;
		int animnum = 0;
		if(TAnimNumber.getText().equals("")) {
			fe = SAnimNo.getValue();
		} else {
			try {
				animnum = Integer.parseInt(TAnimNumber.getText());
			} catch(NumberFormatException ex) {
				ErrorDialogue("Bad animation number");
				return;
			}
			for(int i = 0; i < AIRAnimationDescs.length; i++) {
				AIRElement e = AIRAnimationDescs[i];
				if(animnum == e.AnimationNumber) {
					fe = i;
					continue;
				}
			}
			if(fe == -1) {
				InformationDialogue("Not found");
				return;
			}
		}
		SelectedAnimIndex = fe;
		CurrentFrame = 0; //Start from frame0
		FrameTimer = 0;
		TMAnim.start();
	}
	
	//TMAnim timer tick callback
	public void animate() {
		//Selected Animation descriptor
		AIRElement e = AIRAnimationDescs[SelectedAnimIndex];
		//Current frame's animation descriptor
		AIRElementSub es = e.Elements[CurrentFrame];
		//Show image specified in descriptor
		int r = _findimg(es.GroupNo, es.ImageNo);
		if(r != -1) {
			SelectedImageIndex = r;
		} else {
			//If image that specified animation element was not found
			//stop and show error.
			stopAnimation();
			ErrorDialogue(
					String.format("Can not find image having GroupNo %d " + 
							"and ImageNo %d. Animation will halt.", 
							es.GroupNo, es.ImageNo) );
		}
		//Wait for time specified in Disptime
		//(Show same image until Disptime elapses)
		//If display time is -1, it means 'stay'
		if(es.DispTime != -1) { FrameTimer++; }
		if(FrameTimer >= es.DispTime) {
			FrameTimer = 0;
			CurrentFrame++;
			if(CurrentFrame >= e.Elements.length) {
				//Rewind frame specified in Loopstartnum
				CurrentFrame = e.LoopstartNum;
			}
		}
	}
	
	public void stopAnimation() {
		TMAnim.stop();
		SelectedAnimIndex = -1;
	}
	
	//Draw callback: called every 15mS
	public void draw() {
		G.setColor(Color.BLACK);
		G.fillRect(0, 0, Scr.getWidth(), Scr.getHeight());
		G.setColor(Color.WHITE);
		if(SFFImgArray != null) {
			//Show image if SelectedImageIndex is not negative and
			//less than loaded image count
			if(0 <= SelectedImageIndex && 
					SelectedImageIndex < SFFImgArray.length) {
				//Draw image on center of screen
				//e.X and e.Y are selected image's center offset
				SFFElement e = SFFImgArray[SelectedImageIndex];
				int ix = (int)((Scr.getWidth() / 2.0) - e.X);
				int iy = (int)((Scr.getHeight() / 2.0) - e.Y);
				BufferedImage img = ObtainBFImage(SelectedImageIndex, true);
				G.drawImage(img, ix, iy, this);
				//Draw image information
				G.drawString(String.format("Group%d Image%d %dx%d (%d/%d)",
						e.GroupNo, e.ImageNo, e.X, e.Y,
						SelectedImageIndex + 1, SFFImgArray.length), 0, 10);
			} else {
				//Draw image information
				G.drawString(String.format("%d image loaded.",
						SFFImgArray.length), 0, 10);
			}
		}
		if(AIRAnimationDescs != null) {
			int animlen = AIRAnimationDescs.length;
			if(0 <= SelectedAnimIndex && SelectedAnimIndex <= animlen) {
				AIRElement e = AIRAnimationDescs[SelectedAnimIndex];
				//If animation is playing
				G.drawString(
						String.format("Playing animation%d Frame%d/%d", 
								e.AnimationNumber,
								CurrentFrame + 1,
								e.Elements.length),
						0, 20);
			} else {
				//Draw animation information
				G.drawString(String.format("%d animations loaded.",
					AIRAnimationDescs.length), 0, 20);
			}
			G.drawString(
					String.format("Selecting Animation%d by slider (%d/%d)",
					AIRAnimationDescs[SAnimNo.getValue()].AnimationNumber,
					SAnimNo.getValue() + 1,
					AIRAnimationDescs.length), 0, 30);
		}
		PicBox.repaint();
	}
	
	//Called when PictureBox was resized
	public void componentResized(ComponentEvent e) {
		//Resize screen image and get new graphics obj
		InitCanvas(PicBox.getWidth(), PicBox.getHeight());
	}
	
	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	//Image index slider state changed callback
	public void stateChanged(ChangeEvent e) {
		stopAnimation();
		if(SFFImgArray == null) {
			SelectedImageIndex = -1;
		} else {
			SelectedImageIndex = SImgIndex.getValue();
		}
	}
}
