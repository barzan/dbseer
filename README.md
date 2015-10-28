DBSeer Installation and Usage Guide

*Watch our video demo*: http://dbseer.org/video
---

**0. Package Overview**

DBSeer consists of two sub-packages: 1) *middleware* and 2) *GUI front-end*.

The middleware is located under the 'middleware' directory and the GUI is located under the 'dbseer_front_end' directory.

DBSeer's internal logic is written in MATLAB (or GNU Octave 4.0.0 or higher) and Julia. The middleware and GUI front-end are both written in Java.

Note: In this documentation we will refer to both servers and clients. The server refers to the machines running your database management system (e.g., MySQL) and/or running DBSeer's middleware. The client is used to refer to the machine (e.g., your laptop) where you will lunch DBSeer's GUI and can manage your database and middleware from.

**1. Dependencies**

You need the following packages on the client that runs DBSeer's GUI:

* MATLAB (R2007b or higher) or Octave (4.0.0 or higher)
* Julia (0.3.10 or higher)
* Java 1.6+

The middleware requires the following packages:

* Java 1.6+
* Python 2.7+ and MySQL-python (for MySQL and MariaDB)

If you are compiling DBSeer from source, then you need the following:

* JDK 1.6+
* ant (to compile the source code)

Your server must be running:

* Linux (to use the middleware which in turn runs '*dstat*'; however, you can use other operating systems if you can run '*dstat*' command on them)
* MySQL, MariaDB or Postgres (these are the only DBMSs that are currently supported)

**2. Building DBSeer from the source code**

Check out the latest release (from https://github.com/barzan/dbseer/) and then follow the instructions below:

***2.1. Building the middleware***

To build the middleware, go to the root directory of the middleware, and run the file named 'build', as shown below

	./build

If the the file is not executable, use the following command to make it executable:

	chmod 755 build

***2.2. Bulding the GUI front-end***

The DBSeer GUI front-end has been developed using IntelliJ IDEA. 

* Import the directory 'dbseer_front_end' as a project in IntelliJ IDEA. 
* Manually compile the package.

To manually compile the GUI, follow the following steps:

The GUI front-end includes a xml file for 'ant' to compile itself. In the directory 'dbseer_front_end', run the following command:

	> ant -f dbseer_front_end.xml
	
This will create *dbseer_front_end.jar* in the 'out/artifacts/dbseer_front_end_jar' directory.

**3. Installing DBSeer**

***3.1. Configuring dstat***

To make sure the middleware can execute dstat (to monitor system resources and get the log file), please first configure the variables in rs-sysmon2/setenv. In the setenv file, you need to configure {mysql_user, mysql_pass, mysql_host, mysql_port} variables. Also, make sure that the file rs-sysmon2/dstat is executable. If not, use the following command to make it executable,

	chmod 755 rs-sysmon2/dstat

***3.2. Running the middleware***

To run the middleware, please run the file named '*middleware*' (in the top-level of the middleware directory) with required
arguments, as described below.

If you would like to run DBSeer's middleware on a different server than the one hosting your database: 

	./middleware <listening_port> <User Name>@<DB IP> <DB Port> <Thread Number> <User Password File> <port for user>(optional)

But if you would like to run DBSeer's middleware on the same server that is hosting your database:

	./middleware <listening_port> 127.0.0.1 <DB Port> <Thread Number> <User Password File> <port for user>(optional)

(Here, '127.0.0.1' can also be replaced with 'localhost')

If the the file is not executable, use the following command to make it
executable:

Note: If your database server is located on a remote server, please run the middleware command with root permissions (simply add 'sudo' before the ./middleware commands).

Arguments details:

	<listening_port>:	(an integer) the port number that DBSeer's middleware should listen on. You should specify
				the same port number that your users/applications send their SQL queries to.

	<User Name>@<DB IP>:	(a string) the user name for the middleware
				to login to the server hosting the database (via SSH) and the IP address
				of the server hosting the database 

	<DB Port>:		(an integer) the listening port of the DB. This should be different than the <listening_port> 
				used by DBSeer's middleware if the middleware and the database are on the same server.
				The middleware forwards the incoming queries to the database via this port.

	<Thread Number>:	(an integer) the number of working threads to be used by the middleware. The middleware
				can use several threads to forward the clients queries to the database. default value: ??

	<User Password File>:	(a string) the name of the file that contains users ID and correspoding passwords, with
				database-related variables for dstat (only for scenarios where the database server and the
				middleware are hosted on different servers). The file should follow the format shown below:
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

				Place this file in the top directory of the middleware (by default, named 'Middleware')

	<port for user>:	(an integer) the port number used by the middleware to listen to the GUI front-end requests.
				This argument is optional (by default, it is set to 3334)



***3.3. Installing the statistical package***

You need to install Julia and either Matlab or Octave on the client that you plan to run DBSeer's front-end on. Note that the client you run DBSeer on is typically different than the server(s) you are running your database and middleware on.

To install Julia and Matlab on your client, follow their own documentation (you do not need to install Matlab if you plan to use Octave).

**3.3.1 Notes on Julia**

Julia must be executable from the terminal with the command '*julia*'. Please set the environment variables of your operating systems accordingly so that you can launch Julia from the terminal.

For Mac OS X, you may need to add a symbolic link as follows:

	> sudo ln -s /Applications/Julia-x.x.x.app/Contents/Resources/julia/bin/julia /usr/local/bin/julia

**3.3.2. Octave installation** 

Unlike MATLAB, Octave does not come with a stand-alone installation binary. Depending on your operating system, you may need to manually build and install the package. You can find instructions on how to install Octave for different operating systems at the *Download* section of the Octave homepage (<http://www.gnu.org/software/octave/download.html>). 

For Mac OS X users, the easiest way to install Octave is to install it via [Homebrew](http://brew.sh/). The instructions to install Octave using Homebrew can be found at Octave's wiki [page](http://wiki.octave.org/Octave_for_MacOS_X#Homebrew).

Once Octave is installed in your system, you also need to install the following required packages for DBSeer:

* *io*
* *control*
* *struct*
* *statistics*
* *signal*
* *optim*

The above packages can be installed by executing the following command in Octave (requires an internet connection):

	> pkg install -forge <package_name>
	 
	# for example, to install io package, run:
	> pkg install -forge io


***3.4. Choosing the statistical package***

DBSeer can work with either MATLAB or Octave for its statistical operations. You can specify the statistical package that you want DBSeer to use in a configuration file (on your client) in INI format. 

The 'dbseer.ini' configuration file should have the following format:

	[dbseer]
	; set a statistical package for DBSeer. DBSeer currently supports Matlab (R2007b and greater) and Octave (4.0.0 and higher).
	; set 'matlab' for MATLAB, 'octave' for Octave. Default is 'matlab'.
	stat_package=matlab
	
The default statistical package in DBSeer is Matlab. But if you change the value of *stat_package* to *octave* in the INI file, Octave will be used for DBSeer's statistical operations. 


**4. Running DBSeer**

You can run the jar file to launch the GUI with the command:

	> java -jar dbseer_front_end.jar

OR you can specify the INI configuration file to use as its argument:

	> java -jar dbseer_front_end.jar ./dbseer.ini
	
If you do not specify the INI configuration file, DBSeer will automatically search for the file in the current working directory and use the default configuration values if it cannot find the INI file. A sample INI file can be found in the package as 'dbseer\_front\_end/dbseer.ini'.

To familiarize yourself with various features in DBSeer's GUI, watch the following video:  http://dbseer.org/video

