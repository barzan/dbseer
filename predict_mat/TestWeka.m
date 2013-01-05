function Rw=TestWeka(VData,Class,Text)
%Rw=TestWeka(V,Class,Text)
%Run a Weka model 
%V is the verification or test
%Model is the string with of the Model File (output from the training - Look at Run Weka)
%Classifier  - 3 is Model tree 1 and 2 are linear regresion models
% Class =1 Multilayer
% Class =2 LeastMedsq
% Class =3 Model trees (M5P)
% Class =4 PaceRegresions
% Text = String to identify the results


switch Class
    case 1
        WekaString=char('weka.classifiers.functions.MultilayerPerceptron');
       
    case 2
        WekaString=char('weka.classifiers.functions.LeastMedSq');
    case 3
        WekaString=char('weka.classifiers.trees.M5P');
    case 4
        WekaString=char('weka.classifiers.functions.PaceRegression');
    case 5
        WekaString=char('weka.classifiers.functions.LinearRegression');
    case 6
        WekaString=char('weka.classifiers.functions.SMOreg');
    case 7
        WekaString=char('weka.classifiers.lazy.IBk');
    case 8
        WekaString=char('weka.classifiers.lazy.LWL');
end



S=size(VData,2);
for i=1:S
    attributeName{i}=['V' num2str(i)];
    attributeType{i}='numeric';
end

arffWrite('RunWekaTest.arff','Verification',attributeName,attributeType,VData);
ActualPath=cd;
WekaPath=which('weka.jar');

ModelName=['ModelOut' num2str(Class) '.model'];
ModelOut=['ModelTest' Text '.Out'];

Temp=['java -cp "' WekaPath '" ' WekaString ' -T "' ActualPath '\RunWekaTest.arff" -l ' ModelName ' -p 0 > "' ModelOut '"'];
dos(Temp);

%%
Rw.Y=load(['ModelTest' Text '.Out']);
Rw.ModelName=[ActualPath '\ModelOut.model'];
Rw.Error=Error2(Rw.Y(:,2),VData(:,end));
