% the function receives in input a data file a window size in seconds and
% the nubmer of transaction types
% and returns 2 matrixes containing the number of transactions per type per
% window and the average latency per window



%filename = 'coefs-7200-20-20-20-20-3000';
%winSize=1;
%numVariable=5;
function [roughNumTrans numTrans latencies perc_latencies] = deverticalize(indir, outdir, filename, winSize, numVariable)
    
    tic;
    % load the file skipping the first 4 lines
    temp = csvread(horzcat(indir,filename),4);
    
    fprintf(1, 'READ FROM DISK TIME:');
    toc;
    
    tic;
    % create an empty matrix
    numTrans = zeros(ceil(max(temp(:,2))/(winSize*1000000))-1,numVariable);
    latencies = zeros(ceil(max(temp(:,2))/(winSize*1000000))-1,numVariable);
    perc95_latencies = zeros(ceil(max(temp(:,2))/(winSize*1000000))-1,numVariable);
    % for each winSize second create a window
    
    for i=1:ceil(max(temp(:,2))/(winSize*1000000))
        
        %find the various types of transactions
        rough = find(temp(:,2)/(winSize*1000000)<i+1 & temp(:,2)/(winSize*1000000)>=i); 
        contained_indexes = find(temp(:,2)/(winSize*1000000)<i+1 & temp(:,2)/(winSize*1000000)>=i & (temp(:,2)+temp(:,3))/(winSize*1000000)<i+1); 
        finishing_here_indexes = find(temp(:,2)/(winSize*1000000)<i & (temp(:,2)+temp(:,3))/(winSize*1000000)<i+1 & (temp(:,2)+temp(:,3))/(winSize*1000000)>=i); 
        starting_here_indexes = find(temp(:,2)/(winSize*1000000)<i+1 & temp(:,2)/(winSize*1000000)>=i & (temp(:,2)+temp(:,3))/(winSize*1000000)>i+1);
        passing_here_indexes = find(temp(:,2)/(winSize*1000000)<i & (temp(:,2)+temp(:,3))/(winSize*1000000)>i+1);
        
        for j=1:numVariable
            
           %find per-trans-type indexes 
           rough_id = rough(temp(rough,1)==j);
           contained_id= contained_indexes(temp(contained_indexes,1)==j);
           finish_here_id= finishing_here_indexes(temp(finishing_here_indexes,1)==j);
           starting_here_id= starting_here_indexes(temp(starting_here_indexes,1)==j);
           passing_here_id= passing_here_indexes(temp(passing_here_indexes,1)==j);
           
           %numTrans(i,j) = size(id2,1);
           % add the contained trans completely + portions of the others
           
           
           contained_contribution = size(contained_id,1);
           finishing_contribution = sum(((temp(finish_here_id,2)/(winSize*1000000))+(temp(finish_here_id,3)/(winSize*1000000))-i)./(temp(finish_here_id,3)/(winSize*1000000)));
           starting_contribution = sum((i+1-(temp(starting_here_id,2)/(winSize*1000000)))./(temp(starting_here_id,3)/(winSize*1000000)));
           passing_contribution = sum((winSize*1000000)/temp(passing_here_id,3));
           
           roughNumTrans(i,j) = size(rough_id,1);
           numTrans(i,j) = contained_contribution + finishing_contribution + starting_contribution + passing_contribution;
           latencies(i,j) = mean(temp(rough_id,3));
           perc95_latencies(i,j) = prctile(temp(rough_id,3),95);
           
        end
    end     
    fprintf(1, 'CRUNCH TIME:');
    toc;
    
    fprintf(1, 'SAVE TIME:');
    tic;
    save(horzcat(outdir,filename,'_trans_count'), 'numTrans','-ascii');
    save(horzcat(outdir,filename,'_rough_trans_count'), 'roughNumTrans','-ascii');
    save(horzcat(outdir,filename,'_avg_latency'), 'latencies','-ascii');
    save(horzcat(outdir,filename,'_95th_perc_latency'), 'perc_latencies','-ascii');
    toc;
    
end
