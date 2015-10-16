function barzanLinSolve(responseVars, features)

	model = zeros(size(features,2)+1, size(responseVars,2));
	for i=1:size(responseVars,2)
		model[:,i] = llsq([features ones(size(features,1),1)],responseVars[:,i]);
	end

	return model

end
