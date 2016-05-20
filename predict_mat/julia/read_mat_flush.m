load temp.mat;

if lenOfLegends == 5
	legends = {'Actual', 'LR', 'LR+classification', 'Our model', 'Tree regression'};
elseif lenOfLegends == 4
	legends = {'LR', 'LR+classification', 'Our model', 'Tree regression'};
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
	errorHeader = legends(2:5);
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

title=title_write;
clear title_write;

Xlabel = Xlabel_write;
clear Xlabel_write;

Ylabel = Ylabel_write;
clear Ylabel_write;
