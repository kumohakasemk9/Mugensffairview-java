/*
Copyright (C) 2023 kumohakase (license: https://creativecommons.org/licenses/by-nc/4.0/)
PictureBox class
Please consider supporting me using kofi.com https://ko-fi.com/kumohakase
*/

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class PictureBox extends JPanel {
	BufferedImage img;
	public void ChangeImg(BufferedImage i) {
		img = i;
	}
	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, this);
	}
}
