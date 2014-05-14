function projections = barzanNeuralNetInvoke(model, features)

if isOctave
    projections = sim(model, features');
else
    projections = model(features');
end
    projections = projections';    
    
%perf = perform(model,y,t)

end

