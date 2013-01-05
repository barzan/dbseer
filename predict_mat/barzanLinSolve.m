function model = barzanLinSolve(responseVars, features)

model = zeros(size(features,2)+1, size(responseVars,2));
for i=1:size(responseVars,2)
    model(:,i) = regress(responseVars(:,i), [features ones(size(features,1),1)]);
end    

end

