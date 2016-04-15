% Copyright 2013 Barzan Mozafari
%
% Licensed under the Apache License, Version 2.0 (the "License");
% you may not use this file except in compliance with the License.
% You may obtain a copy of the License at
%
%     http://www.apache.org/licenses/LICENSE-2.0
%
% Unless required by applicable law or agreed to in writing, software
% distributed under the License is distributed on an "AS IS" BASIS,
% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
% See the License for the specific language governing permissions and
% limitations under the License.

function R=RunWeka(TData,VData,Class,Param)
% Copyright Gerald Corzo
% R=RunWeka(TData,VData,Class,Param)
% This program runs the weka sofwate assuming that the weka file weka.jar is
% on one of the paths of matlab
% TData= Training Matrix with column representing variables and row samples
% TData= Verification Matrix with column representing variables and row  samples
% Class =1 Multilayer
% Class =2 LeastMedsq
% Class =3 Model trees (M5P)
% Class =4 PaceRegresions
% Class =5 LinearRegresions
% Class =6 SMOregresions
% Class =7 Intance Based
% Class =8 LWL -> Local weigthed regression
% Output files in the Directory are
% RunWekaT.arff
% RunWekaV.arff
%  ModelName='ModelOut.model';
%  ModelOut='ModelOut.out ';
%


switch Class
    case 1
        WekaString=char('weka.classifiers.functions.MultilayerPerceptron');
        if nargin<4
            Param=' -L 0.3 '; % Leraning Rate
            Param=[Param '-M 0.2 '];%Momentum
            Param=[Param '-N 500 ']%Epochs
            Param=[Param '-H a ']%Automatic hidden node calculation
        else
            Par=Param;
            Param=[];
            Param=[' -L ' num2str(Par.lr)]; % Leraning Rate
            Param=[Param ' -M ' num2str(Par.mu)];%Momentum
            Param=[Param ' -N ' num2str(Par.Epochs)]%Epochs
            Param=[Param ' -H ' num2str(Par.nodes)]%Automatic hidden node calculation
        end
    case 2
        WekaString=char('weka.classifiers.functions.LeastMedSq');
        Param=[];
    case 3
        WekaString=char('weka.classifiers.trees.M5P');
        Param=[' -M ' num2str(Param)];
    case 4
        WekaString=char('weka.classifiers.functions.PaceRegression');
        Param=[];%' -S 4';
    case 5
        WekaString=char('weka.classifiers.functions.LinearRegression');
        Param=[];
    case 6
        WekaString=char('weka.classifiers.functions.SMOreg');
        C=Param.C; % C is the penalty parameter of the error term - Lagrange, in this case also is called in weka as: The complexity parameter
        E=Param.E; % Exponential when is a polinomial kernel
        G=Param.gamma;
        RBF=Param.RBF;
        if RBF==0
            Param=[' -S 0.0010 -C ' num2str(C) ' -E ' num2str(E) ' -G ' num2str(G) ' -A 250007 -T 0.0010 -P 1.0E-12 -N 0'];
        else
            Param=[' -S 0.0010 -C ' num2str(C) ' -E ' num2str(E) ' -G ' num2str(G) ' -A 250007 -T 0.0010 -P 1.0E-12 -N 0 -R']; 
        end
    case 7 
        WekaString=char('weka.classifiers.lazy.IBk');
        try
            Param=[' -K ' num2str(Param) ' -W 0'];
        catch
            msgbox( 'Probably you missed the number of instance');
        end
    case 8 
        WekaString=char('weka.classifiers.lazy.LWL');
        Param=[' -U 0 -K ' num2str(Param) ' -W weka.classifiers.trees.DecisionStump'];
end

S=size(TData,2);
for i=1:S
    attributeName{i}=['V' num2str(i)];
    attributeType{i}='numeric';
end


WekaPath=which('weka.jar');

arffWrite(['RunWekaT' num2str(Class) '.arff'],'Training',attributeName,attributeType,TData);
arffWrite(['RunWekaV' num2str(Class) '.arff'],'Verification',attributeName,attributeType,VData);
ActualPath=cd;


ModelName=['ModelOut' num2str(Class) '.model'];
ModelOut=['ModelOut' num2str(Class) '.out'];

Temp=['java -cp "' WekaPath '" ' WekaString ' -t "' ActualPath '\RunWekaT' num2str(Class) '.arff" -T "' ActualPath '\RunWekaV' num2str(Class) '.arff" -d ' ModelName Param ' -p 0 > "' ModelOut '"'];

dos(Temp);


%%
%%Calculating RMSE
R.Y=load(['ModelOut' num2str(Class) '.Out']);
R.ModelName=[ActualPath '\ModelOut' num2str(Class) '.model'];
R.Error=Error2(R.Y(:,2),VData(:,end));
