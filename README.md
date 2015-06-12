DBSeer Installation and Usage Guide
---

The current DBSeer includes two sub-packages: 1) *middleware* and 2) *GUI front-end*.

The middleware works independently from DBSeer main and located under directory 'middleware'. The GUI works with both DBSeer main and middleware. The GUI is located under directory 'dbseer_front_end'

The main DBSeer package is written in MATLAB, and also supports Octave (4.0.0 and higher). The middleware and GUI front-end are written in Java.

**1. Dependencies**

* MATLAB (R2007b and higher) or Octave (4.0.0 and higher)
* JDK 1.6+
* (for middleware) Linux ('dstat' only runs on Linux)
* ant (to compile the GUI front-end manually)

**2. Middleware**

NOTE: All commands in this section assume you are in the root directory of the middleware. 

A) Building the Middleware

To build the middleware, please run the file named 'build', as shown below

	./build

If the the file is not executable, use the following command to make it
executable:

	chmod 755 build
	
B) Executing the Middleware

To execute the middleware, please run the file named 'middleware' with required
arguments, as shown below

	./middleware <listening_port> <User Name>@<MySQL IP> <MySQL Port>
		<Thread Number> <User Password File> <port for user>(optional)
	(for remote MySQL IP)

or

	./middleware <listening_port> 127.0.0.1 <MySQL Port> <Thread Number>
		<User Password File> <port for user>(optional)
	(for local MySQL IP, '127.0.0.1' can be replaced with 'localhost')

If the the file is not executable, use the following command to make it
executable:

	chmod 755 build

If MySQL server is located on remote IP, please run the middleware with root
privilege (simply add 'sudo' before the command line above).

Arguments details:

	<listening_port>:	an integer as port number used by middleware to
				listening to MySQL clients requests

	<User Name>@<MySQL IP>:	a string contains the user name for middleware
				to login remote server via SSH and the IP address
				of remote server

	<MySQL Port>:		an integer as listening port number of MySQL
				server

	<Thread Number>:	an integer which tells the program to create
				that number of working threads, which pass
				MySQL clients queries and log them, and this
				number excludes the thread used to communicate
				with users.

	<User Password File>:	a string as name of the file which contains
				users ID and correspoding passwords, with
				MySQL-related variables for dstat (only for the
				remote MySQL server scenario. The file should
				follow the format shown below

					<user-password>
					user0
					password0
					user1
					password1
					(and so on...)
					</user-password>
					mysql_user=user_name
					mysql_pass=password
					mysql_host="127.0.0.1"
					mysql_port=3306

				Please put this file in the top directory of
				middleware, by default, named 'Middleware'

	<port for user>:	an integer as port number used by middleware to
				listening to user (GUI front-end) requests. This argument is
				optional, by default, it is set to 3334

C) Configure dstat if monitoring local MySQL server

To make sure the middleware execute dstat to monitor system resources and get
the log file, please first configure the variables in rs-sysmon2/setenv. In the setenv file, you need to configure {mysql_user, mysql_pass, mysql_host, mysql_port} variables. Also, please make sure the file rs-sysmon2/dstat is executable. If not, use the
following command to make it executable,

	chmod 755 rs-sysmon2/dstat

D) Configure dstat if monitoring remote MySQL server

To make sure the middleware is able to deploy and execute dstat on remote server
to monitor system resources and get the log file, please first configure the
variables in dstat_for_server/setenv. In the setenv file, you need to configure {mysql_user, mysql_pass, mysql_host, mysql_port} variables. 

E) Dependencies

* javac (version 1.6+)
* java (version 1.6+)
* GNU BASH
* Linux (for 'dstat')

**3. GUI front-end**

The DBSeer GUI front-end has been developed using IntelliJ IDEA. 

There are two ways to run the GUI:

* Import the directory 'dbseer_front_end' as a project in IntelliJ IDEA and run the GUI. 
* Manually compile the package and run the generated jar file. 

A) Manual compilation

The GUI front-end includes a xml file for 'ant' to compile itself. In the directory 'dbseer_front_end', you can run

	> ant -f dbseer_front_end.xml
	
to compile and it will create *dbseer_front_end.jar* in the directory 'out/artifacts/dbseer_front_end_jar'

You can run the jar file to launch the GUI with the command:

	> java -jar dbseer_front_end.jar

OR you can specify the INI configuration file to use as its argument:

	> java -jar dbseer_front_end.jar ./dbseer.ini
	
If you do not specify the INI configuration file, DBSeer will automatically search for the file in the current working directory and use the default configuration values if it does not exist. A sample INI file can be found at 'dbseer_front_end/dbseer.ini'.
	
B) Setting the statistical package

DBSeer requires MATLAB or Octave for its mathematical calculations. By default, DBSeer uses MATLAB as its statistical package. You can specify the statistical package that DBSeer uses in a separate configuration file in INI format. 

The 'dbseer.ini' configuration file has the following format:

	[dbseer]
	; set a statistical package for DBSeer. DBSeer currently supports Matlab (R2007b and greater) and Octave (4.0.0 and higher).
	; set 'matlab' for MATLAB, 'octave' for Octave. Default is 'matlab'.
	stat_package=matlab
	
You can change the value of *stat_package* to *octave* and Octave will be used for DBSeer's statistical operations.

C) MATLAB interaction

Upon its execution, the GUI will automatically launch MATLAB in order to interact with the DBSeer engine. 
This is done with *matlabcontrol* (<https://code.google.com/p/matlabcontrol/>).

D) Octave installation

Unlike MATLAB, Octave does not come with a stand-alone installation binary that works for every operating system. Depending on the operating system, a user may need to manually build and install the package.

DBSeer requires the version of Octave that is 4.0.0 or higher. You can find instructions on how to install Octave for each operating system at the *Download* section of the Octave homepage (<http://www.gnu.org/software/octave/download.html>).

E) Octave interaction

Likewise, the interaction with Octave has been implemented with JavaOctave (<https://kenai.com/projects/javaoctave/pages/Home>).