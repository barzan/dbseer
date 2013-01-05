function avgDirty = trueMcDirtyPages(uniquePages, TPS, seconds)

pages = zeros(1, uniquePages);
requests = TPS*seconds;

avgDirty = 0;

nIterations = 100000;

for iter=1:nIterations
    pages(:) = 0;
    for r=1:requests 
        p = randsample(uniquePages,1);
        pages(p) = 1; 
    end
    dirty = length(find(pages));
    avgDirty = dirty + avgDirty;
end

avgDirty = avgDirty / nIterations; 

end

