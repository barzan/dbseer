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

function [ P, Card, rowSize ] = loadKeyDistributionFiles()


%Tables!
ipblocks=1;
logging=2;
page=3;
page_backup=4;
page_restrictions=5;
recentchanges=6;
revision=7;
text=8;
user=9;
user_groups=10;
value_backup=11;
watchlist=12;

tableNames = {'ipblocks','logging','page','page_backup','page_restrictions','recentchanges','revision','text','user','user_groups','value_backup','watchlist'};
nTables = length(tableNames);

rowSize = zeros(1, nTables);

rowSize(ipblocks)=16384;
rowSize(logging)=136;
rowSize(page)=156;
rowSize(page_backup)=156;
rowSize(page_restrictions)=630;
rowSize(recentchanges)=151;
rowSize(revision)=94;
rowSize(text)=8549;
rowSize(user)=202;
rowSize(user_groups)=59;
rowSize(value_backup)=8192;
rowSize(watchlist)=115;

Card = zeros(1, nTables);

%%%%%%Transactions!

WIKI_ADD_WATCHLIST=1;
WIKI_REMOVE_WATCHLIST=2;
WIKI_SELECT_PAGE=3;
WIKI_UPDATE_PAGE_1=4;
WIKI_UPDATE_PAGE_2=5;
WIKI_UPDATE_PAGE_3=6;
WIKI_UPDATE_PAGE_4=7;

transTypes = {'WIKI_ADD_WATCHLIST','WIKI_REMOVE_WATCHLIST','WIKI_SELECT_PAGE','WIKI_UPDATE_PAGE_1','WIKI_UPDATE_PAGE_2','WIKI_UPDATE_PAGE_3','WIKI_UPDATE_PAGE_4'};
nTransTypes=length(transTypes);

overallExecutedTrans = 26770;

executedTransactions = zeros(1, nTransTypes);

for tran=1:nTransTypes
    try
        filename = char(strcat('OVERALL__', transTypes(tran), '_write'));
        A = csvread(filename,1);
        executedTransactions(tran) = A(1,2);
    catch err
        executedTransactions(tran) = 0;
    end
end    
overallExecutedTrans = 26770;
overallExecutedTrans = sum(executedTransactions);

maxCard = 154881;

pRead = zeros(nTransTypes, nTables, maxCard);
pWrite = zeros(nTransTypes, nTables, maxCard);

for tab=1:nTables
    for tran=1:nTransTypes
        filename = char(strcat(tableNames(tab),'_',transTypes(tran),'_read'));
        Aread = csvread(filename,1);
        
        %if sum(Aread(:,2))>0
        %    filename = char(strcat(tableNames(tab),'_',transTypes(tran),'_read_overallProbability'))
        %    AreadOverAll = csvread(filename,1);            
        %end
        
        filename = char(strcat(tableNames(tab),'_',transTypes(tran),'_write'));
        Awrite = csvread(filename,1);        
        %if sum(Awrite(:,2))>0
        %    filename = char(strcat(tableNames(tab),'_',transTypes(tran),'_write_overallProbability'))
        %    AwriteOverAll = csvread(filename,1);
        %end
        n1=size(Aread,1);
        n2=size(Awrite,1);
        if n1~=n2
            fprintf(1,'What the heck? %s : %d and %d\n', filename, n1, n2);
        end
        nRows = max(n1, n2);
        if nRows > maxCard
            error('Problem in pre-allocating the memory!');
        end
        Card(tab) = nRows;
        pRead(tran, tab, 1:nRows) = Aread(:,2);
        s1 = sum(Aread(:,2));
        pWrite(tran, tab, 1:nRows) = Awrite(:,2);        
        s2 = sum(Awrite(:,2));
        s3 = executedTransactions(tran);
        fprintf(1,'%s tran %d> %d %d %d\n', char(tableNames(tab)), tran, s1, s2, s3);
        
        %Now let's turn them into a probability distribution!
        if s1~=0
            pRead(tran, tab, 1:nRows) = pRead(tran, tab, 1:nRows) / s1;
            if s1 < s3
                pRead(tran, tab, 1:nRows) = pRead(tran, tab, 1:nRows) * s1/s3;
            end
        end
        
        if s2~=0
            pWrite(tran, tab, 1:nRows) = pWrite(tran, tab, 1:nRows) / s2;
            if s2 < s3
                pWrite(tran, tab, 1:nRows) = pWrite(tran, tab, 1:nRows) * s2/s3;
            end

        end       
        
    end
end

%Correction
Card(text) = Card(revision);
pRead(:,text,:) = pRead(:,revision,:);
pWrite(:,text,:) = pWrite(:,revision,:);

%Saving!
    f = executedTransactions / overallExecutedTrans;
    save('mixture','f');
    save('read-pattern.mat', 'pRead', 'Card', 'rowSize');
    save('write-pattern.mat', 'pWrite', 'Card', 'rowSize');
end

