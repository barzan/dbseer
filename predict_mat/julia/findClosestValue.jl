function findClosestValue(function_handler, inputRange, value, conf)

	lb = 1;
	ub = size(inputRange, 1);

	iter = 1;
	closest = 0;

	#%if feval(function_handler, conf, inputRange(ub,:)) < value || feval(function_handler, conf, inputRange(lb,:)) > value
	if function_handler == "cfFlushRateApprox"
		if cfFlushRateApprox(conf, inputRange[ub,:])[1] < value    
			closest = Inf;
			return closest
		end    
		if cfFlushRateApprox(conf, inputRange[lb,:])[1] > value
			closest = -Inf;
			return closest
		end

		while ub - lb > 100
			closest = round((ub+lb)/2);
			#%fprintf(1,'Checking %d\n', closest);
			iter = iter + 1;
			cv = cfFlushRateApprox(conf, inputRange[closest,:]);
			if cv[1] < value
				lb = closest;
			else
				if cv[1] > value
					ub = closest;
				else # equals!
					lb = closest;
					ub = closest;
				end
			end
		end
	elseif function_handler == "getConcurrencyLevel"
		if getConcurrencyLevel(conf, inputRange[ub,:])[1] < value    
			closest = Inf;
			return closest
		end    
		if getConcurrencyLevel(conf, inputRange[lb,:])[1] > value
			closest = -Inf;
			return closest
		end

		while ub - lb > 100
			closest = round((ub+lb)/2);
			#%fprintf(1,'Checking %d\n', closest);
			iter = iter + 1;
			cv = getConcurrencyLevel(conf, inputRange[closest,:]);
			if cv[1] < value
				lb = closest;
			else
				if cv[1] > value
					ub = closest;
				else # equals!
					lb = closest;
					ub = closest;
				end
			end
		end
	end
	closest = round((ub+lb)/2);

	println(iter)
	return closest
end #end of function
