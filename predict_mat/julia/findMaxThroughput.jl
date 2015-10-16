function findMaxThroughput(SubmittedTransactions)
	sm1 = 1000; #%1000: lower part, 5000:middle
	sm2 = 10;
	mslope = 10; #%lower this number sooner it declares a max throughput!

	sTPS = DoSmooth(SubmittedTransactions, sm1);
	#%sTPS = smooth(SubmittedTransactions, sm1);
	#%sTPS = SubmittedTransactions;

	
	Y,I = localmax(sTPS')
	I = sort(I)
	if length(I) == 0
		tmp = []
	else
		tmp = diff(vec(DoSmooth(I,sm2)))
	end

	ind = -1

	for i=1:length(tmp)
		if tmp[i]<mslope
			ind = i
			break
		end
	end
	if ind == -1 || ind+1 > length(I)
		xMaxThroughput = [];
	else
		xMaxThroughput = I[ind + 1];
	end
	yMaxThroughput = sTPS[xMaxThroughput];
	return xMaxThroughput,yMaxThroughput

end
