package window_restorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.swt.widgets.Shell;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Dao to save and get windows size/coordinates
 * @author avonva
 *
 */
public abstract class RestoreableWindowDao {

	/**
	 * Save the window size and coordinates and if it is maximized or not.
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
		config.asObject().get(pref.getCode()).asObject()
			.set("x", pref.getX())
			.set("y", pref.getY())
			.set("w", pref.getWidth())
			.set("h", pref.getHeight())
			.set("max", pref.isMaximized());
		
		// save them
		save(config);
	}
	
	/**
	 * Save a new configuration into the config file
	 * @param config
	 * @throws IOException
	 */
	private void save(JsonValue config) throws IOException {
		
		// save the changes
		try(FileWriter writer = new FileWriter(getConfigFile());) {
			config.writeTo(writer);
			writer.close();
		}
	}
	
	private JsonValue getJsonFile() throws IOException {
		
		File configFile = getConfigFile();
		
		// if not found, create it from scratch
		if (!configFile.exists()) {
			JsonObject file = Json.object();
			save(file);
		}
		
		// read it and return it
		JsonValue config = Json.parse(new FileReader(configFile));
		
		return config;
	}

	/**
	 * Get a window preference using its code.
	 * @param code the code of the preference
	 * @return the preference if it is found, otherwise null
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public RestoreableWindow get(Shell shell, String code) throws FileNotFoundException, IOException {

		JsonValue config = getJsonFile();
		JsonValue window = config.asObject().get(code);

		// if no pref found, return null
		if (window != null) {
			
			JsonObject windowData = window.asObject();
			
			int x = windowData.getInt("x", 0);
			int y = windowData.getInt("y", 0);
			int w = windowData.getInt("w", 200);
			int h = windowData.getInt("h", 200);
			boolean max = windowData.getBoolean("max", false);
			RestoreableWindow pref = new RestoreableWindow(shell, code, x, y, w, h, max);
			return pref;
		}
		
		return null;
	}

	/**
	 * Get the name of the table where the preferences will be stored
	 * @return
	 */
	public abstract File getConfigFile();
}
