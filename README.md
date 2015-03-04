DBSeer Installation and Usage Guide
---

The current DBSeer includes two sub-packages: 1) *middleware* and 2) *GUI front-end*.

The middleware works independently from DBSeer main and located under directory 'middleware'. The GUI works with both DBSeer main and middleware. The GUI is located under directory 'dbseer_front_end'

The main DBSeer package is written in MATLAB. The middleware and GUI front-end are written in Java.

You require the following to run DBSeer:

* MATLAB
* JDK 1.6+ (for the middleware) 
* Linux ('dstat' only runs on Linux)
* ant (to compile the GUI front-end manually)
* common-libs (can be obtained from here https://github.com/barzan/common-libs)

**1. DBSeer**

A) INSTALLATION: You need to add the following directories to your MATLAB path: common_mat predict_data predict_mat sc
Note that the common_mat directory in the common-libs package (https://github.com/barzan/common-libs)

B) How to use DBSeer

You can use the rs-sysmon2 to collect data. For quick evaluation, you can use some of the example datasets that are provided under the example_data directory.

The main entry to DBSeer is through one of the following scripts:

	predict_mat/load_and_plot.m
	predict_mat/predictionConsole.m

To see how to use these scripts, you can run the provided demo example.
To run the demo, you need to go to the example_data/mysql5
	
	MATLAB> cd INSTALL_DIRECTORY/example_data/mysql5

where you replace the INSTALL_DIRECTORY with the path in which you have a copy of DBSeer installation.

Then, assuming you have added predict_mat to MATLAB path, you can simply run the following in MATLAB prompt:
	
	MATLAB> demo

C) Contact info

Please report all bugs or questions to Barzan Mozafari (<mozafari@umich.edu>)

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
the log file, please first configure the variables in rs-sysmon2/setenv. Also,
please make sure the file rs-sysmon2/dstat is executable. If not, use the
following command to make it executable,

	chmod 755 rs-sysmon2/dstat

D) Configure dstat if monitoring remote MySQL server

To make sure the middleware is able to deploy and execute dstat on remote server
to monitor system resources and get the log file, please first configure the
variables in dstat_for_server/setenv.

E) Dependencies

* javac (version 1.6+)
* java (version 1.6+)
* GNU BASH
* Linux (for 'dstat')

**3. GUI front-end**

The DBSeer GUI front-end is developed with IntelliJ IDEA 13. 

There are two ways to run the GUI:

* Import the directory 'dbseer_front_end' as a project in IntelliJ IDEA and run the GUI. 
* Manually compile the package and run the generated jar file. 

A) Manual compilation

The GUI front-end includes a xml file for 'ant' to compile itself. In the directory 'dbseer_front_end', you can run

	> ant -f dbseer_front_end.xml
	
to compile and it will create *dbseer_front_end.jar* in the directory 'out/artifacts/dbseer_front_end_jar'

You can run the jar file to launch the GUI with the command:

	> java -jar dbseer_front_end.jar

B) MATLAB interaction

Upon its execution, the GUI will automatically launch MATLAB in order to interact with the DBSeer engine. 
This is done with *matlabcontrol* (<https://code.google.com/p/matlabcontrol/>)
