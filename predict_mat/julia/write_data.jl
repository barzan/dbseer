file = matopen("temp.mat","w")

lenOfLegends=length(legends)
write(file,"lenOfLegends",lenOfLegends)

for i=1:lenOfLegends
	write(file,string("legends",i),legends[i])
end

lenOfXdata=length(Xdata)
write(file,"lenOfXdata",lenOfXdata)
for i=1:lenOfXdata
	write(file,string("Xdata",i),float(Xdata[i]))
end

lenOfYdata=length(Ydata)
write(file,"lenOfYdata",lenOfYdata)
for i=1:lenOfYdata
	write(file,string("Ydata",i),float(Ydata[i]))
end

lenOfMeanAbsError=length(meanAbsError)
write(file,"lenOfMeanAbsError",lenOfMeanAbsError)
for i=1:lenOfMeanAbsError
	write(file,string("meanAbsError",i),float(meanAbsError[i]))
end

lenOfMeanRelError=length(meanRelError)
write(file,"lenOfMeanRelError",lenOfMeanRelError)
for i=1:lenOfMeanRelError
	write(file,string("meanRelError",i),float(meanRelError[i]))
end

lenOfErrorHeader=length(errorHeader)
write(file,"lenOfErrorHeader",lenOfErrorHeader)

lenOfExtra=length(extra)
write(file,"lenOfExtra",lenOfExtra)
for i=1:lenOfExtra
	write(file,string("extra",i),extra[i])
end

write(file,"title_write",title)
write(file,"Xlabel_write",Xlabel)
write(file,"Ylabel_write",Ylabel)

close(file)

