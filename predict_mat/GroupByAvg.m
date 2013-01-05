function [groupedMatrix freuqencies] = GroupByAvg(matrix, keyIndices, relativeKeyDiff, minFreq, minKeyValue, maxKeyValue)

    idx = find(sum(matrix(:,keyIndices),2)>=minKeyValue);
    matrix = matrix(idx,:);
    
    idx = find(sum(matrix(:,keyIndices),2)<=maxKeyValue);
    matrix = matrix(idx,:);
    
    bigMatrix = sortrows([matrix(:,keyIndices) matrix]);
    matrix = bigMatrix(:,length(keyIndices)+1:end);
    key = matrix(:,keyIndices);       
    nRows = size(key,1);
        
    groupedMatrix = zeros(nRows, size(matrix,2));
    freuqencies = zeros(nRows,1);
    
    groupIdx = 1;
    freq = 0;    
    keySt = 1;
    
    for i=1:nRows
        if all(key(i,:) <= key(keySt,:)+relativeKeyDiff*key(keySt,:))
            freq = freq +1;
        else
            if freq >= minFreq
               groupedMatrix(groupIdx,:) = mean(matrix(keySt:i-1,:), 1); 
               freuqencies(groupIdx) = freq;
               groupIdx = groupIdx+1;
            end
            freq = 1;
            keySt = i;
        end            
    end
    if freq >= minFreq
       groupedMatrix(groupIdx,:) = mean(matrix(keySt:nRows,:), 1);
       freuqencies(groupIdx) = freq;
       groupIdx = groupIdx+1;
    end

    groupedMatrix = groupedMatrix(1:groupIdx-1,:);
    freuqencies = freuqencies(1:groupIdx-1);    

return; 

