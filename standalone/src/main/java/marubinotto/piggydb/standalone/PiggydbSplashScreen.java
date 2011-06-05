package marubinotto.piggydb.standalone;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SplashScreen;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PiggydbSplashScreen {
	
	private static Log logger = LogFactory.getLog(PiggydbSplashScreen.class);

	private SplashScreen splashScreen;
	private BufferedImage splashImage;
	private Graphics2D splashG;
	
	public static int TEXT_OFFSET = 5;

	public PiggydbSplashScreen(SplashScreen splashScreen, String piggydbVersion) 
	throws Exception {
		if (splashScreen == null) throw new IllegalArgumentException("splashScreen");
		
		this.splashScreen = splashScreen;
		
		// Create a base image with version info
		this.splashImage = ImageIO.read(this.splashScreen.getImageURL());
		Graphics2D imageG = this.splashImage.createGraphics();
		imageG.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
		imageG.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
		imageG.setColor(Color.DARK_GRAY);
		imageG.drawString(
			"v" + piggydbVersion, 
			TEXT_OFFSET, 
			(int)this.splashScreen.getSize().getHeight() - TEXT_OFFSET);
		
		// Configure the splash graphics
		this.splashG = splashScreen.createGraphics();
		this.splashG.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
		this.splashG.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
		this.splashG.setColor(Color.GRAY);
		this.splashG.drawImage(this.splashImage, 0, 0, null);
		
		this.splashScreen.update();
	}
	
	protected PiggydbSplashScreen() {
	}
	
	public static PiggydbSplashScreen NULL = new PiggydbSplashScreen() {
		@Override public void message(String message) { }
		@Override public void close() { }
	};
	
	public void message(String message) {
		this.splashG.drawImage(this.splashImage, 0, 0, null);		// Clear the previous message
		
		FontMetrics fm = this.splashG.getFontMetrics();
		int width = fm.stringWidth(message);
		
		this.splashG.drawString(
			message, 
			(int)this.splashScreen.getSize().getWidth() - width - TEXT_OFFSET, 
			(int)this.splashScreen.getSize().getHeight() - TEXT_OFFSET);
		
		this.splashScreen.update();
		
		logger.debug(message);
	}
	
	public void close() {
		this.splashScreen.close();
		this.splashImage.flush();
	}
}
