A) INSTALLATION:

1. You need to download the following libraries and add them to your mathlab's path:

Common-Libs:
	https://github.com/barzan/common-libs



2. You need to add the following directories to your MATLAB path:
predict_data
predict_mat
sc


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
Please report all bugs or questions to Barzan Mozafari <barzan@csail.mit.edu>


