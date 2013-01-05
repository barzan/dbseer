nTypes = 5;
nColumns = 26;
header = 'transType,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26';

f = [0.2 0.2 0.2 0.2 0.2];
howManyRows = 1000;

features = zeros(howManyRows, nColumns+1);

transTypes = randsample(nTypes, howManyRows, true, f);
for i=1:howManyRows
   t = transTypes(i);
   features(i,:) = [t carloWikiTransType(t)];
end

produceWekaFile(header,features, horzcat('features-',num2str(howManyRows),'.csv'));
