function barzanLinInvoke(model, features)

	predictions = [features ones(size(features,1),1)] * model;
	return predictions

end
