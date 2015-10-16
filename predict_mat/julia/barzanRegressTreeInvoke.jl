function barzanRegressTreeInvoke(model, features)
	#% Invokes the regression tree for the given set of features
	features=features''
	predictions = zeros(size(features,1),1)
	for i=1:size(features,1)
		predictions[i]= apply_tree(model,vec(features[i,:]))
	end
	return predictions
end
