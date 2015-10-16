function Div(A, B )
	C = A ./ B;
	if typeof(B) <: Array
		for i=1:length(C)
			if (A[i] == 0) & (B[i] == 0)
				C[i] = 0
			end
		end
	elseif typeof(B) <: Number
		for i=1:length(C)
			if (A[i] == 0) & (B == 0)
				C[i] = 0
			end
		end
	end
	return C
end
