function Max(K)
	M = -Inf;
	len=length(K)
	for i=1:len
		if K[i]>M
			M=K[i]
		end
	end
	return M
end
