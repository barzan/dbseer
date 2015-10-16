load temp.mat

title = 'Max Throughput Prediction';
Ylabel = 'TPS';
Xlabel = 'Time';

legends=cell(1,lenOfLegends);
for i=1:lenOfLegends
	eval(sprintf('tmpv=legends%d;',i));
	if tmpv==1
		legends{i}='Original Signal';
	elseif tmpv==2
		legends{i}='Actual Max Throughput';
	elseif tmpv==3
		legends{i}='Max Throughput based on adjusted LR for CPU+classification';
	elseif tmpv==4
		legends{i}='Max Throughput on LR for CPU+classification';
	elseif tmpv==5
		legends{i}='Max Throughput on adjusted LR for CPU';
	elseif tmpv==6
		legends{i}='Max Throughput based on LR for CPU';
	elseif tmpv==7
		legends{i}='Max Throughput based on our flush rate model';
	elseif tmpv==8
		legends{i}='Max Throughput based on LR for flush rate';
	elseif tmpv==9
		legends{i}='Max Throughput based on our contention model';
	elseif tmpv==10
		legends{i}='Signal Generated From User Input';
	end
	eval(sprintf('clear legends%d;',i));
end
clear lenOfLegends

Xdata=cell(1,lenOfXdata);
for i=1:lenOfXdata
	eval(sprintf('Xdata{%d}=Xdata%d;',i,i));
	eval(sprintf('clear Xdata%d;',i));
end
clear lenOfXdata;

Ydata=cell(1,lenOfYdata);
for i=1:lenOfYdata
	eval(sprintf('Ydata{%d}=Ydata%d;',i,i));
	eval(sprintf('clear Ydata%d;',i));
end
clear lenOfYdata;

if lenOfMeanAbsError == 0
	meanAbsError=cell(0,0);
else
	meanAbsError=cell(1,lenOfMeanAbsError);
end
for i=1:lenOfMeanAbsError
	eval(sprintf('meanAbsError{%d}=meanAbsError%d;',i,i));
	eval(sprintf('clear meanAbsError%d;',i));
end
clear lenOfMeanAbsError;

if lenOfMeanRelError == 0
	meanRelError=cell(0,0);
else
	meanRelError=cell(1,lenOfMeanRelError);
end
for i=1:lenOfMeanRelError
	eval(sprintf('meanRelError{%d}=meanRelError%d;',i,i));
	eval(sprintf('clear meanRelError%d;',i));
end
clear lenOfMeanRelError;

if lenOfErrorHeader == 0
	errorHeader = cell(0,0);
else
	errorHeader = cell(1,lenOfErrorHeader);
end
for i=1:lenOfErrorHeader
	eval(sprintf('tmpv=errorHeader%d;',i));
	if tmpv==3
		errorHeader{i}='Max Throughput based on adjusted LR for CPU+classification';
	elseif tmpv==4
		errorHeader{i}='Max Throughput on LR for CPU+classification';
	elseif tmpv==5
		errorHeader{i}='Max Throughput on adjusted LR for CPU';
	elseif tmpv==6
		errorHeader{i}='Max Throughput based on LR for CPU';
	elseif tmpv==7
		errorHeader{i}='Max Throughput based on our flush rate model';
	elseif tmpv==8
		errorHeader{i}='Max Throughput based on LR for flush rate';
	elseif tmpv==9
		errorHeader{i}='Max Throughput based on our contention model';
	end
	eval(sprintf('clear errorHeader%d;',i));
end
clear lenOfErrorHeader;

if lenOfExtra == 0
	extra=cell(0,0);
else
	extra=cell(1,lenOfExtra);
end
for i=1:lenOfExtra
	eval(sprintf('extra{%d}=extra%d;',i,i));
	eval(sprintf('clear extra%d;',i));	
end
clear lenOfExtra;
