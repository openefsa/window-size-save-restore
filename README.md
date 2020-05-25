<p align="center">
	<img src="http://www.efsa.europa.eu/profiles/efsa/themes/responsive_efsa/logo.png" alt="European Food Safety Authority"/>
</p>

# Window size save and restore
This Maven project module, written in Java, can be used for restoring the size and the coordinates of SWT Shells (GUI windows of the SWT library) which were set by the final user of the application, in order to improve the usability of the tool.
In particular, these information are stored into an SQL database.

## Dependencies
All project dependencies are listed in the [pom.xml](pom.xml) file.

## Import the project
In order to import the project correctly into the integrated development environment (e.g. Eclipse), it is necessary to download the project together with all its dependencies.
The project and all its dependencies are based on the concept of "project object model" and hence Apache Maven is used for the specific purpose.
In order to correctly import the project into the IDE it is firstly required to create a parent POM Maven project (check the following [link](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html) for further information). 
Once the parent project has been created add the project and all the dependencies as "modules" into the pom.xml file as shown below: 

	<modules>

		<!-- dependency modules -->
		<module>module_1</module>
		...
		...
		...
		<module>module_n</module>
		
	</modules>
	
Next, close the IDE and extract all the zip packets inside the parent project.
At this stage you can simply open the IDE and import back the parent project which will automatically import also the project and all its dependencies.

_Please note that the "SWT.jar" and the "Jface.jar" libraries (if used) must be downloaded and installed manually in the Maven local repository since are custom versions used in the tool ((install 3rd party jars)[https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html])._

## How to use the library
* Identify a class which represents a GUI window of the application that you want to save and restore;
* Create inside this class a RestoreableWindow object and Initialise it with the SWT shell you want to save and with an unique code that is used to identify the window in the database.
* Use the window.saveOnClosure method to say that the shell size and coordinates should be saved into the DB when the shell will be closed
* Use the window.restore method to restore the size and coordinates of the shell which are stored in the database

## Example

The following class is our GUI window for the Java application (using the SWT library):

		public class MyWindow {
		
			public MyWindow(Shell parentShell) {
				Display display = new Display();
				Shell shell = new Shell(display);
				shell.open();
			}	
		}
		
We first need to add a RestoreableWindow object and specify which is the unique code that identifies the window in our DB, in order to keep track of the changes and to restore the shell size.

		public class MyWindow {
		
			private RestoreableWindow window;                      // window to restore the shell size
			private final static String WINDOW_CODE = "MyWindow";  // code for the DB
			
			public MyWindow(Shell parentShell) {
				Display display = new Display();
				Shell shell = new Shell(display);
				shell.open();
				
				window = new RestoreableWindow(shell, WINDOW_CODE);
			}
		}
		
Then, we need to specify that we want to save and restore the shell dimensions:

		public class MyWindow {
		
			private RestoreableWindow window;                      // window to restore the shell size
			private final static String WINDOW_CODE = "MyWindow";  // code for the DB
			
			public MyWindow(Shell parentShell) {
			
				Display display = new Display();
				Shell shell = new Shell(display);
				shell.open();
				
				window = new RestoreableWindow(shell, WINDOW_CODE);
				
				// restore the old dimensions of the window fetching
				// the information from the database
				window.restore(MyWindowPreferenceDao.class);
				
				// save this window dimensions when it is closed
				// into the database
				window.saveOnClosure(MyWindowPreferenceDao.class);
			}
		}

As you can see, for the methods restore and saveOnClosure it is needed to specify a dao class. In fact, this class contains the information of where the
preference file (JSON) storing all the windows information should be stored (you will need to create it extending the already provided WindowPreferenceDao class 
and providing the file where the JSON preference file will be stored)

Let's have a look to the MyWindowPreferenceDao class:

	public class MyWindowPreferenceDao extends RestoreableWindowDao {

		public static final String WINDOWS_SIZES_FILENAME = "config/windows-sizes.json";
		
		@Override
		public File getConfigFile() {
			return new File(WINDOWS_SIZES_FILENAME);
		}
	}

As you can see, the getConfigFile function returns the file where the information will be stored, in this case in the config folder in the windows-sizes.json file.
