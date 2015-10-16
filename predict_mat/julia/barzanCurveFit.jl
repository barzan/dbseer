function barzanCurveFit(lockType, trainX, trainY, lowConf, upConf, minStep, maxSteps)
	totalSteps = 0;
	conf = 0;
	if !(size(lowConf, 1) == 1 && size(upConf, 1) == 1 && size(minStep, 1) == 1 && size(maxSteps, 1) == 1)
		error("Config should be a row-vector");
	end
	if !(size(trainY,2)==1)
		error("We only accept 1-D as target values");
	end

	lowVal = implicitUseLockModel(lockType,lowConf, trainX);
	upVal = implicitUseLockModel(lockType,upConf, trainX);

	for col=1:size(lowConf,2)
		steps = 0;
		while abs(upConf[col]-lowConf[col]) > minStep[col] && steps <= maxSteps[col]
			steps = steps+1;
			totalSteps = totalSteps+1;
		
			lowErr = mean(abs(lowVal-trainY));
			if isnan(lowErr) 
				lowConf[col] = (lowConf[col] + upConf[col])/2;
				lowVal = implicitUseLockModel(lockType,lowConf, trainX);
				continue;
			end 
		
			upErr = mean(abs(upVal-trainY));
			if isnan(upErr) 
				upConf[col] = (lowConf[col] + upConf[col])/2;
				upVal = implicitUseLockModel(lockType,upConf, trainX);
				continue;
			end 
		
			if lowErr>upErr
				lowConf[col] = (lowConf[col] + upConf[col])/2;
				lowVal = implicitUseLockModel(lockType,lowConf, trainX);
			else
				upConf[col] = (lowConf[col] + upConf[col])/2;
				upVal = implicitUseLockModel(lockType,upConf, trainX);
			end
			println(string("steps=",steps,", low=",valueToString(lowConf)", up=",valueToString(upConf),", lowErr=",lowErr,", upErr=",upErr));
		end
	    
		if (steps > maxSteps[col])
			println(string("Reached the maximum number of iterations: ", steps));
		else
			println(string("Converged after ", steps, " iterations for this column"));
		end
	    
		conf = (upConf + lowConf)/2;
	end

	println(string("Overall number of steps: ", totalSteps, " iterations"));
	return conf
end
