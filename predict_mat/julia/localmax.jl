function localmax(s)
	Y = zeros(size(s))
	I = {}
	for i=2:length(s)-1
		if s[i]>s[i-1] && s[i]>=s[i+1]
			Y[i] = 1
			push!(I,i)
		end
	end
	return Y,I
end
