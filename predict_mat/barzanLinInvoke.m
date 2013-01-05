function predictions = barzanLinInvoke(model, features)

predictions = [features ones(size(features,1),1)] * model;

end

