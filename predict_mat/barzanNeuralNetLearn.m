function model = barzanNeuralNetLearn(responseVars, features)

if isOctave
    min_max_elem = min_max(features');
    MLPnet = newff(min_max_elem, [10 1], {'tansig', 'purelin'}, 'trainlm', '', 'mse');
    model = train(MLPnet, features', responseVars');
else
    model = fitnet(10);

    model.trainParam.showWindow = false;
    model.trainParam.showGUI = false;

    model = train(model, features', responseVars');
end

        
%view(model)
%y = model(x);
%perf = perform(model,y,t)

end

