package window_restorer;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface to be used in order to save the shell dimensions on the database.
 * You can use the {@link WindowPreference} class to save and restore the window
 * settings of a {@link RestoreableWindow} class. Use
 * {@link WindowPreference#restore} to restore the settings of a previously
 * saved window and use
 * {@link WindowPreference#saveOnClosure(RestoreableWindow)} to save the
 * settings of a window when it is closed
 * 
 * @author avonva
 * @author shahaal
 *
 */
public class RestoreableWindow {
	
	private static final Logger LOGGER = LogManager.getLogger(RestoreableWindow.class);

	private Shell shell;
	private String code;
	private int x;
	private int y;
	private int width;
	private int height;
	private boolean maximized;

	public RestoreableWindow(Shell shell, String code) {
		this.code = code;
		this.shell = shell;
		getSizeFromShell();
	}

	/**
	 * @param shell the shell we want to save the size
	 * @param code  Code to identify the window. Note that if two windows have the
	 *              same code, some overwrites can happen.
	 * @return
	 */
	protected RestoreableWindow(Shell shell, String code, int x, int y, int w, int h, boolean max) {
		this.shell = shell;
		this.code = code;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.maximized = max;
	}

	/**
	 * Restore the shell size which was saved previously.
	 * 
	 * @param daoClass
	 * @param window
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	public boolean restore(Class<? extends RestoreableWindowDao> daoClass) {

		// get the preference related to the window passed
		// as input
		RestoreableWindowDao windDao;
		try {
			windDao = daoClass.newInstance();
		} catch (IllegalAccessException | InstantiationException e1) {
			LOGGER.error("There was a problem accessing the window", e1);
			e1.printStackTrace();
			return false;
		}

		RestoreableWindow pref = null;
		try {
			pref = windDao.get(shell, this.code);
		} catch (IOException e) {
			LOGGER.error("There was a problem accessing the window", e);
			e.printStackTrace();
		}

		// either if we got an exception or if no pref was found
		if (pref == null)
			return false;

		// if we are using a single screen
		// adjust the variables using the
		// screen bounds
		if (this.isSingleScreen())
			this.adjustToSingleScreen();

		// get the parameters and restore them
		int x = pref.getX();
		int y = pref.getY();
		int width = pref.getWidth();
		int height = pref.getHeight();
		boolean max = pref.isMaximized();

		shell.setLocation(x, y);
		shell.setSize(width, height);
		shell.setMaximized(max);
		shell.layout();

		return true;
	}

	/**
	 * Save the dimensions parameters of the shell when it will be closed. The
	 * parameters are stored in the database and will be usable calling
	 * {@link WindowPreference#restore(RestoreableWindow)}.
	 * 
	 * @param shell
	 */
	public void saveOnClosure(Class<? extends RestoreableWindowDao> daoClass) {

		// when the shell is closed
		shell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent arg0) {

				getSizeFromShell();

				// insert(or update) the window preference
				RestoreableWindowDao windDao;
				try {
					windDao = daoClass.newInstance();
					windDao.update(RestoreableWindow.this);
				} catch (InstantiationException | IllegalAccessException | IOException e) {
					LOGGER.error("There was a problem accessing the window", e);
					e.printStackTrace();
				}
			}
		});
	}

	private void getSizeFromShell() {

		// get the new size of the shell
		x = shell.getLocation().x;
		width = shell.getSize().x;

		y = shell.getLocation().y;
		height = shell.getSize().y;

		maximized = shell.getMaximized();

		// if we are using a single screen
		// adjust the variables using the
		// screen bounds
		if (isSingleScreen())
			adjustToSingleScreen();
	}

	protected void setCode(String code) {
		this.code = code;
	}

	protected void setX(int x) {
		this.x = x;
	}

	protected void setY(int y) {
		this.y = y;
	}

	protected void setHeight(int height) {
		this.height = height;
	}

	protected void setWidth(int width) {
		this.width = width;
	}

	protected void setMaximized(boolean maximized) {
		this.maximized = maximized;
	}

	/**
	 * Get the code of the window preference
	 * 
	 * @return
	 */
	public String getCode() {
		return code;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isMaximized() {
		return maximized;
	}

	/**
	 * Detect if there is a single screen or if two or more screens are used
	 * together
	 * 
	 * @return
	 */
	private boolean isSingleScreen() {

		// get all the screens
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		return gs.length == 1;
	}

	/**
	 * Adjust the coordinates based on the main screen dimensions, in order to
	 * prevent the window to appear outside of the screen.
	 */
	private void adjustToSingleScreen() {

		// set the maximum limit of the screen
		if (x < 0)
			x = 0;

		if (y < 0)
			y = 0;

		// get the screen dimensions in pixels
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		if (y + height > dim.getHeight())
			y = (int) (dim.getHeight() - height);

		if (x + width > dim.getWidth())
			x = (int) (dim.getWidth() - width);

	}

	@Override
	public String toString() {
		return "Restoreable window: shell=" + shell + ",x=" + x + ",y=" + y + ",width=" + width + ",height=" + height
				+ ",maximized=" + maximized;
	}
}
