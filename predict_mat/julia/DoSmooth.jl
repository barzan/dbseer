function DoSmooth(a, l)
	if l % 2 == 1
		l = l - 1
	else
		l = l - 2
	end
	b = cumsum(a)
	Z = zeros(size(a))
	tmpLen = length(a)
	for i=1:tmpLen
		len = min(l/2,tmpLen-i,i-1)
		Z[i]=(b[i+len]-b[i-len]+a[i-len])/(2*len+1)
	end
	return Z
end
