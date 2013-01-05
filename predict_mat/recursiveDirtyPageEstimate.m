function estimatedDirtyPages = recursiveDirtyPageEstimate(domainCardinality, data)
    dirtyPagesAtTheFirstPoint=data(1,3);
    rowsChangedPerSecond = data(:,1);
    pagesFlushed = data(:,2);
    
    estimatedDirtyPages = zeros(size(rowsChangedPerSecond,1),1);
    
    estimatedDirtyPages(1) = dirtyPagesAtTheFirstPoint;
    for i=1:size(data,1)-1
        estimatedDirtyPages(i+1) = estimatedDirtyPages(i) + mapRowsToPages(domainCardinality-estimatedDirtyPages(i), rowsChangedPerSecond(i)*(domainCardinality-estimatedDirtyPages(i))/domainCardinality) - pagesFlushed(i); 
    end    
end

