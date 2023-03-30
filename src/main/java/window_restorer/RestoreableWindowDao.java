package window_restorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Shell;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Dao to save and get windows size/coordinates
 * 
 * @author avonva
 * @author shahaal
 *
 */
public abstract class RestoreableWindowDao {
	
	private static final Logger LOGGER = LogManager.getLogger(RestoreableWindowDao.class);

	private static final int POINT_X = 0;
	private static final int POINT_Y = 0;
	private static final int LENGTH_W = 500;
	private static final int LENGTH_H = 500;
	
	/**
	 * Save the window size and coordinates and if it is maximized or not.
	 * 
	 * @param pref
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void update(RestoreableWindow pref) throws FileNotFoundException, IOException {

		// get json file
		JsonValue config = getJsonFile();

		// search for the window
		JsonValue window = config.asObject().get(pref.getCode());

		// create the node if not present
		if (window == null)
			window = config.asObject().set(pref.getCode(), Json.object());

		// add the new information
		config.asObject().get(pref.getCode()).asObject().set("x", pref.getX()).set("y", pref.getY())
				.set("w", pref.getWidth()).set("h", pref.getHeight()).set("max", pref.isMaximized());

		// save them
		save(config);
	}

	/**
	 * Save a new configuration into the config file
	 * 
	 * @param config
	 * @throws IOException
	 */
	private void save(JsonValue config) throws IOException {

		// save the changes
		try (FileWriter writer = new FileWriter(getConfigFile())) {
			config.writeTo(writer);
			writer.close();
		}
	}

	/**
	 * the method return a json file solved memory leak if the filereader has not
	 * been closed
	 * 
	 * @author shahaal
	 * @return
	 * @throws IOException
	 */
	private JsonValue getJsonFile() throws IOException {

		File configFile = getConfigFile();

		// if not found, create it from scratch
		if (!configFile.exists() || !isValidJson(new FileReader(configFile))) {
			JsonObject file = Json.object();
			save(file);
		}

		// read it and return it
		JsonValue config = null;

		// solve memory leak
		try (FileReader fr = new FileReader(configFile)) {
			config = Json.parse(fr);
		}

		return config;
	}

	/**
	 * the method is used to check if the given json file is valid or not if not
	 * valid the default file is created
	 * 
	 * @author shahaal
	 * @return
	 * @throws IOException
	 */
	private boolean isValidJson(FileReader fr) {
		try {
			Json.parse(fr);
			return true;
		} catch (Exception e) {
			LOGGER.error("There was a problem parsing json", e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Get a window preference using its code.
	 * 
	 * @param code the code of the preference
	 * @return the preference if it is found, otherwise null
	 * @throws SQLException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public RestoreableWindow get(Shell shell, String code) throws FileNotFoundException, IOException {

		JsonValue config = getJsonFile();
		JsonValue window = config.asObject().get(code);

		// if there are predefined values for the given code
		if (window != null) {

			JsonObject windowData = window.asObject();

			int x = windowData.getInt("x", POINT_X);
			int y = windowData.getInt("y", POINT_Y);
			int w = windowData.getInt("w", LENGTH_W);
			int h = windowData.getInt("h", LENGTH_H);
			boolean max = windowData.getBoolean("max", false);
			
			return new RestoreableWindow(shell, code, x, y, w, h, max);
			
		} else {
			LOGGER.info("No window preference found related to code " + code);
			return new RestoreableWindow(shell, code);
		}

	}

	/**
	 * Get the name of the table where the preferences will be stored
	 * 
	 * @return
	 */
	public abstract File getConfigFile();
}
