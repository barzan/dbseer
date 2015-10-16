file = matopen("temp.mat","w")
#title: "Max Throughput Prediction"

lenOfLegends=length(legends)
write(file,"lenOfLegends",lenOfLegends)

for i=1:lenOfLegends
	write(file,string("legends",i), legends[i])
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

#Ylabel = "TPS";
#Xlabel = "Time";

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
for i=1:lenOfErrorHeader
	if errorHeader[i]=="Max Throughput based on adjusted LR for CPU+classification"
		write(file,string("errorHeader",i),3)
	elseif errorHeader[i]=="Max Throughput on LR for CPU+classification"
		write(file,string("errorHeader",i),4)
	elseif errorHeader[i]=="Max Throughput on adjusted LR for CPU"
		write(file,string("errorHeader",i),5)
	elseif errorHeader[i]=="Max Throughput based on LR for CPU"
		write(file,string("errorHeader",i),6)
	elseif errorHeader[i]=="Max Throughput based on our flush rate model"
		write(file,string("errorHeader",i),7)
	elseif errorHeader[i]=="Max Throughput based on LR for flush rate"
		write(file,string("errorHeader",i),8)
	elseif errorHeader[i]=="Max Throughput based on our contention model"
		write(file,string("errorHeader",i),9)
	end
end

lenOfExtra=length(extra)
write(file,"lenOfExtra",lenOfExtra)
for i=1:lenOfExtra
	write(file,string("extra",i),extra[i])
end

close(file)
