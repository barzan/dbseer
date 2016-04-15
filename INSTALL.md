# DBSeer Installation Guide

## 0. Package Overview

DBSeer consists of two sub-packages: 1) *middleware* and 2) *GUI front-end*.

The middleware can be found at this github [page](https://github.com/dongyoungy/dbseer_middleware).  The GUI is located under the 'dbseer_front_end' directory.

DBSeer's internal logic is written in MATLAB (or GNU Octave 4.0.0 or higher) and Julia. The middleware and GUI front-end are both written in Java.

Note: In this documentation we will refer to both servers and clients. The server refers to the machines running your database management system (e.g., MySQL) and/or running DBSeer's middleware. The client is used to refer to the machine (e.g., your laptop) where you will launch DBSeer's GUI and can manage your database and middleware from.

## 1. Dependencies

Note: You can run DBSeer from a Docker image, which is a lightweight VM with every dependency installed. If you are interested in running DBSeer this way. Please refer to **Docker Usage Guide for DBSeer.pdf**

You need the following packages on the client that runs DBSeer's GUI:

* MATLAB (R2007b or higher) or Octave (4.0.0 or higher)
* Julia (0.3.10 or higher)
* Java 1.7+

The middleware requires the following packages:

* Java 1.7+
* Python 2.7+ and MySQL-python (for MySQL and MariaDB)

If you are compiling DBSeer from source, then you need the following:

* JDK 1.7+
* ant (to compile the source code)

Your server must be running:

* Linux (to use the middleware which in turn runs '*dstat*'; however, you can use other operating systems if you can run '*dstat*' command on them)
* MySQL, MariaDB or Postgres (these are the only DBMSs that are currently supported)

## 2. Building DBSeer from the source code

Check out the latest release (from https://github.com/barzan/dbseer/) and then follow the instructions below:

### 2.1. Bulding the GUI front-end

The DBSeer GUI front-end has been developed using IntelliJ IDEA. 

* Import the directory 'dbseer_front_end' as a project in IntelliJ IDEA. 
* Manually compile the package.

To manually compile the GUI, follow these steps:

The GUI front-end includes a xml file for 'ant' to compile itself. In the directory 'dbseer_front_end', run the following command:

	> ant -f dbseer_front_end.xml
	
This will create *dbseer_front_end.jar* in the 'out/artifacts/dbseer_front_end_jar' directory.

## 3. Installing DBSeer

### 3.1. Installing the statistical package

You need to install Julia and either Matlab or Octave on the client that you plan to run DBSeer's front-end on. Note that the client you run DBSeer on is typically different than the server(s) you are running your database and middleware on.

To install Julia and Matlab on your client, follow their own documentation (you do not need to install Matlab if you plan to use Octave).

**3.1.1 Notes on Julia**

Julia must be executable from the terminal with the command '*julia*'. Please set the environment variables of your operating systems accordingly so that you can launch Julia from the terminal.

For Mac OS X, you may need to add a symbolic link as follows:

	> sudo ln -s /Applications/Julia-x.x.x.app/Contents/Resources/julia/bin/julia /usr/local/bin/julia

**3.1.2. Octave installation** 

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


### 3.4. Configuration

The following is the sample `dbseer.ini`:

```
[dbseer]
; set a statistical package for DBSeer. DBSeer currently supports Matlab (R2007b and greater) and Octave (4.0.0 and higher).
; set 'matlab' for MATLAB, 'octave' for Octave. Default is 'matlab'.
stat_package=matlab
; specify the type of target database. Currently only support 'mysql'.
database=mysql
; specify the type of target OS. Currently only support 'linux'.
os=linux
; specify the number of transactions required to start clustering.
; this is also the minumum number of transactions that DBSeer requires to create a dataset from monitoring data.
dbscan_init_pts=1000

; section for mysql-related configurations
[mysql]
; set a delimiter for transaction log. Must match the log_delimiter value in performancelogroute in MaxScale. (default: :::)
log_delimiter=:::
; set a query delimiter for transaction log. Must match the query_delimiter value in performancelogroute in MaxScale. (default: @@@)
query_delimiter=@@@
```
	
**stat_package**

DBSeer can work with either MATLAB or Octave for its statistical operations. `stat_package` specifies the statistical package that you want DBSeer to use.
The default statistical package in DBSeer is Matlab. But if you change the value of `stat_package` to *octave* in the INI file, Octave will be used for DBSeer's statistical operations. 

**database**

`database` specifies the type of DBMS at the server. Currently DBSeer only supports *mysql* (i.e., mysql, mariadb).

**os**

`os` specifies the type of OS at the server. Currnetly DBSeer only supports *linux*.

**dbscan\_init\_pts**

`dbscan_init_pts` specifies the minimum number fo transactions that DBSeer requires for transaction clustering during its live monitoring.

**log\_delimiter**

`log_delimiter` specifies a delimiter for transaction log. It must match the *log\_delimiter* value for **performancelogroute** in MaxScale.

**query\_delimiter**

`query_delimiter` specifies a query delimiter for transaction log. It must match the *query\_delimiter* value for **performancelogroute** in MaxScale.


## 4. Running DBSeer

You can run the jar file to launch the GUI with the command:

	> java -jar dbseer_front_end.jar

OR you can specify the INI configuration file to use as its argument:

	> java -jar dbseer_front_end.jar ./dbseer.ini
	
If you do not specify the INI configuration file, DBSeer will automatically search for the file in the current working directory and use the default configuration values if it cannot find the INI file. A sample INI file can be found in the package as 'dbseer\_front\_end/dbseer.ini'.

To familiarize yourself with various features in DBSeer's GUI, watch the following video:  http://dbseer.org/video

For a detailed usage guide of DBSeer, please refer to *'DBSeer Usage Guide.pdf'* in the repository.

