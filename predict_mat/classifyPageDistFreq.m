function [ output_args ] = classifyPageDistFreq( input_args )


[PP IX] = sort(PPwrite,2,'descend');
freq = FreqWrite;
for i=1:size(freq,1)
    freq(i,:)=FreqWrite(i,IX(i,:));
end
% Now PP is the sorted version of PPwrite and freq is also sorted along
% with PP to keep the correspondence between PP and freq.


nClusters=1;
startIdx =1;
for i=2:D
   if sum(abs(PP(:,startIdx)-PP(:,i))) + sum(abs(freq(:,startIdx)-freq(:,i)))> tolerance
       nClusters = nClusters +1;
       startIdx = i;
   end
end
%now we know that we need to have 'cluster' number of buckets!
nClusters
newPP = zeros(size(PP,1), nClusters);
newFreq = zeros(size(freq,1), nClusters);
counts = zeros(1, nClusters);

curCluster=1;
startIdx =1;
newPP(:,curCluster) = PP(:,1);
newFreq(:,curCluster) = freq(:,1);
counts(curCluster) = 1;

for i=2:D
   if sum(abs(PP(:,startIdx)-PP(:,i))) + sum(abs(freq(:,startIdx)-freq(:,i))) > tolerance
       curCluster = curCluster +1;
       startIdx = i;
       newPP(:,curCluster) = PP(:,i);
       newFreq(:,curCluster) = freq(:,i);
       counts(curCluster) = 1;
   else
       newPP(:,curCluster) = newPP(:,curCluster) + PP(:,i);
       newFreq(:,curCluster) = newFreq(:,curCluster) + freq(:,i);
       counts(curCluster) = counts(curCluster) + 1;
   end
end

for i=1:size(newPP,1)
    newPP(i,:) = newPP(i,:) ./ counts;
    newFreq(i,:) = newFreq(i,:) ./ counts;
end

PP = newPP;
freq = newFreq;


end

