function model = barzanNeuralNetLearn(responseVars, features)

model = fitnet(10);

model.trainParam.showWindow = false;
model.trainParam.showGUI = false;

model = train(model, features', responseVars');
%view(model)
%y = model(x);
%perf = perform(model,y,t)

end

