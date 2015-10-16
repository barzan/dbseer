file = matopen("temp.mat","w")
#title:"Lock Prediction"
#write(file,"title",title)

#length:6 -> {"Actual", "Our contention model", "LR+class", "quad+class", "Dec. tree regression", "Orig. Thomasian"};
#length:5 -> {"Our contention model", "LR+class", "quad+class", "Dec. tree regression", "Orig. Thomasian"};
lenOfLegends=length(legends)
write(file,"lenOfLegends",lenOfLegends)

#=
for i=1:lenOfLegends
	write(file,string("legends",i),legends[i])
end
=#

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

#Xlabel = "TPS";
#Ylabel = "Total time spent acquiring row locks (seconds)";
#=
write(file,"Xlabel",Xlabel)
write(file,"Ylabel",Ylabel)
=#

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

#length>0 -> {"Our contention model", "LR+class", "quad+class", "Dec. tree regression", "Orig. Thomasian"};
lenOfErrorHeader=length(errorHeader)
write(file,"lenOfErrorHeader",lenOfErrorHeader)
#=
for i=1:lenOfErrorHeader
	write(file,string("errorHeader",i),errorHeader[i])
end
=#

lenOfExtra=length(extra)
write(file,"lenOfExtra",lenOfExtra)
for i=1:lenOfExtra
	write(file,string("extra",i),extra[i])
end
close(file)

#=
file = matopen("lockP2.mat","w")
write(file,"title",title)
write(file,"legends",legends)
write(file,"Xdata",Xdata)
write(file,"Ydata",Ydata)
write(file,"Xlabel",Xlabel)
write(file,"Ylabel",Ylabel)
write(file,"meanAbsError",meanAbsError)
write(file,"meanRelError",meanRelError)
write(file,"errorHeader",errorHeader)
write(file,"extra",extra)
close(file)
=#

#=
matwrite("lockP.mat",{
	"title" => title,
	"legends" => legends,
	"Xdata" => Xdata,
	"Xlabel" => Xlabel,
	"Ylabel" => Ylabel,
	"meanAbsError" => meanAbsError,
	"meanRelError" => meanRelError,
	"errorHeader" => errorHeader,
	"extra" => extra
})
=#
