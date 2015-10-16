load temp.mat;

title='lock Prediction';

if lenOfLegends == 6
	legends = {'Actual', 'Our contention model', 'LR+class', 'quad+class', 'Dec. tree regression', 'Orig. Thomasian'};
elseif lenOfLegends == 5
	legends = {'Our contention model', 'LR+class', 'quad+class', 'Dec. tree regression', 'Orig. Thomasian'};
end
clear lenOfLegends;

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

Xlabel = 'TPS';
Ylabel = 'Total time spent acquiring row locks (seconds)';

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
	errorHeader = legends(2:6);
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
