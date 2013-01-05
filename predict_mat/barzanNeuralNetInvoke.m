function projections = barzanNeuralNetInvoke(model, features)

projections = model(features');
projections = projections';
%perf = perform(model,y,t)

end

