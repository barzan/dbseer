function  mae(predictions, actualdata )
	# mean absolute error
	if (size(predictions,1)>1 && size(predictions,2)>1) || (size(actualdata,1)>1 && size(actualdata,2)>1)
		out = mean(abs(predictions-actualdata),1)
	else
		out = mean(abs(predictions-actualdata))
	end
	return out
end
