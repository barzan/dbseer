function closest = findClosestValue(function_handler, inputRange, value, conf)

lb = 1;
ub = size(inputRange, 1);

iter = 1;

%if feval(function_handler, conf, inputRange(ub,:)) < value || feval(function_handler, conf, inputRange(lb,:)) > value
if feval(function_handler, conf, inputRange(ub,:)) < value    
    closest = Inf;
    return
end    
if feval(function_handler, conf, inputRange(lb,:)) > value
    closest = -Inf;
    return
end

while ub - lb > 100
    closest = round((ub+lb)/2);
    %fprintf(1,'Checking %d\n', closest);
    iter = iter + 1;
    cv = feval(function_handler, conf, inputRange(closest,:));
    if cv < value
        lb = closest;
    else
        if cv > value
            ub = closest;
        else % equals!
            lb = closest;
            ub = closest;
        end
    end
end

closest = round((ub+lb)/2);

iter 
end

