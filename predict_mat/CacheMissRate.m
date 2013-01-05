function avgMissRates = CacheMissRate(conf, transCounts)
persistent pageLocation

scaling = conf(1,1);
howManyMBs = conf(1,2);

myseed=1;
RandStream.setDefaultStream(RandStream('mt19937ar','seed',myseed));

buffer_pool_size = 1024 * 1024 * howManyMBs; 
pageSize = 16 * 1024; % 16 KB
bufferPoolSlots = buffer_pool_size / pageSize;
earlistInsertionIdxWhenFull = (5/8) * bufferPoolSlots;

overallTime = tic;

%%%%%%%%%%

avgMissRates = zeros(size(transCounts,1),1);

[uniqueTransCounts smallIdx bigIdx]= unique(transCounts, 'rows');
uniqueMissRates = zeros(size(uniqueTransCounts,1),1);

load('tpcc-access');
PPPaccess = PPPaccess * scaling;
PPPaccess(PPPaccess>1) = 1;
D = size(PPPaccess, 2);
w = zeros(1, stepStarts(end)-1);

NULL = dlnode(-1);
%pageLocation = [NULL];

%pageLocation = repmat(pageLocation, 1, D);
%save('pageLocation.mat', 'pageLocation');
%for i=1:D
%    pageLocation = [pageLocation NULL];
%end

recencyLength = 10000;

%%initialization %%%%%%%%
for uniqueId=1:size(uniqueTransCounts,1)
    avgBuffer = zeros(1, recencyLength);
    pageLocation = [NULL];
    pageLocation = repmat(pageLocation, 1, D);
    save('pageLocation.mat', 'pageLocation');

    curTransCounts = uniqueTransCounts(uniqueId,:);
    tps = sum(curTransCounts);
    
    for i=1:size(curTransCounts,2)
        w(stepStarts(i):stepStarts(i+1)-1) = curTransCounts(i); 
    end
    w = w / sum(w);
    finalP = w * PPPaccess; 
    save('finalP.txt','finalP','-ASCII');
    
    return;
    
    nRounds = 0;
    avgM = 0;
    oldAvgM = 1e10; barzanAvgM = 0;
    epsilon = 0.000001;    
    
    head = [];
    middle = [];
    tail = [];
    numberOfFullSlots = 0;    
    ok = [];
    while abs(avgM-oldAvgM)>0.0000000001 || nRounds < 100000
%    while abs(avgM-oldAvgM)>0.1 || nRounds < 10
        oldAvgM = avgM;

        %Put the actual stuff from here:
        %draw all the pages for this second!
        thisSecondPages = randsample(D, tps,true, finalP);
        number_of_misses = 0;
        number_of_hits = 0;
        for tranId=1:tps % start of this second!
            %choose a page
            pageId = thisSecondPages(tranId);
            if pageLocation(pageId).Data==-1 %% doesn't exist in the cache
                number_of_misses = number_of_misses + 1;
                if numberOfFullSlots==bufferPoolSlots % the cache is full, delete the last one
                    % delete the last one
                    node = tail;
                    tail = node.getPrev; %update tail
                    node.disconnect;
                    pageLocation(node.Data) = NULL;
                    % use this deleted node for the new page
                    node.Data = pageId;
                    pageLocation(pageId) = node;
                    % insert this new node before the middle one
                    node.insertBefore(middle);
                    % update middle
                    middle = node;                
                else % there is room for this new page
                    node = dlnode(pageId);
                    pageLocation(pageId) = node;
                    if numberOfFullSlots < earlistInsertionIdxWhenFull
                        %add this node to the end and update tail
                        node.belowMiddle = false;
                        if isempty(tail)
                            tail = node;
                            head = node;
                        else
                            node.insertAfter(tail);
                        end
                        tail = node;
                        if numberOfFullSlots == earlistInsertionIdxWhenFull -1
                            node.belowMiddle = true;
                            if ~isempty(middle)
                                error('middle should have been null.');
                            end
                            middle = node;
                        end
                    else % i.e. we already have a middle point
                        % insert this new node before the middle one
                        node.belowMiddle = true;
                        node.insertBefore(middle);
                        % update middle
                        middle = node;                
                    end
                    numberOfFullSlots = numberOfFullSlots + 1;
                end
            else %exists
                number_of_hits = number_of_hits + 1;
                if pageLocation(pageId).belowMiddle % exists and is below the middle line
                    % update the middle
                    middle = middle.getPrev;
                    middle.belowMiddle = true;
                    % disconnect this node
                    node = pageLocation(pageId);
                    if isempty(node.getPrev)
                        head = node.getNext;
                    end
                    if isempty(node.getNext)
                        tail = node.getPrev;
                    end                
                    node.disconnect;
                    % update head
                    node.belowMiddle = false;
                    node.insertBefore(head);
                    head = node;                
                else % exists and is above the middle line
                    % disconnect this node
                    node = pageLocation(pageId);
                    if ~isempty(node.getPrev) % otherwise this node is already our head we don't need to move anything!
                        if isempty(node.getNext) % since we are moving the tail we need to  find a new tail!
                            tail = node.getPrev;
                        end
                        node.disconnect;
                        % update head
                        node.insertBefore(head);
                        head = node;                
                    end
                end
            end

            %fprintf(1, 'we read %d\n', pageId);
            %showList(head);
        
        end % end of this second!
            
        thisSecondsMissRate = number_of_misses / tps;
        
        barzanAvgM = (nRounds*barzanAvgM + thisSecondsMissRate) / (nRounds+1)
        
        nRounds = nRounds+1;
        avgBuffer(mod(nRounds, recencyLength)+1) = thisSecondsMissRate;
        % update the running average thus far
        if nRounds<recencyLength
            avgM = sum(avgBuffer) / nRounds;
        else
            avgM = sum(avgBuffer) / recencyLength; 
        end   

        if mod(nRounds,1000)==0
            nRounds
            fprintf(1, 'barzanAvgM=%f, avgM=%f, thisSecondsMissRate=%f\n', barzanAvgM, avgM, thisSecondsMissRate);
        end
        ok = [ok; thisSecondsMissRate];
        
    end
    save('ok.mat','ok');
    %plot(allf(1:n),'-');
    uniqueMissRates(uniqueId) = avgM;
    
end % for the for over different TPSs

for i=1:size(transCounts,1)
    avgMissRates(i) = uniqueMissRates(bigIdx(i))
end

elapsed = toc(overallTime);
fprintf(1,'CacheMissRate elapsed time=%f\n',elapsed);


end

