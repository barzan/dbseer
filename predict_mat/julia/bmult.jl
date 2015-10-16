function bmult(x, y)
	if x==0 && !isnan(y)
		val = 0;
	else
		if y==0 && !isnan(x)
			val = 0;
		else
			val = x * y;
		end
	end
	return val
end
