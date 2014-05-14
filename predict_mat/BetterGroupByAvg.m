function [groupedMatrix freuqencies] = BetterGroupByAvg(matrix, keyIndices, howManyClusters, minFreq, minKeyValue, maxKeyValue)
    nClusters = howManyClusters;

    idx = find(sum(matrix(:,keyIndices),2)>=minKeyValue);
    matrix = matrix(idx,:);
    
    idx = find(sum(matrix(:,keyIndices),2)<=maxKeyValue);
    matrix = matrix(idx,:);
    
    key = matrix(:,keyIndices);       

    %mySeed = 1363;
    %RandStream.setGlobalStream(RandStream('mt19937ar','seed', mySeed));
    %[IDX Centroids] = kmeans(key, nClusters, 'emptyaction','drop');
    [IDX Centroids] = kmeans(key, nClusters, 'emptyaction','singleton');

    groupedMatrix = zeros(nClusters, size(matrix,2));
    freuqencies = zeros(nClusters,1);
    
    actualNClusters = 0;
    
    for i=1:nClusters
        cluster = matrix(IDX==i,:);
        if size(cluster,1) < minFreq
            continue
        end
        actualNClusters = actualNClusters + 1;
        groupedMatrix(actualNClusters,:) = mean(cluster);
        freuqencies(actualNClusters) = size(cluster,1);
    end
    groupedMatrix = groupedMatrix(1:actualNClusters,:);
    freuqencies = freuqencies(1:actualNClusters);    

return; 

