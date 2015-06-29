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

function [PPread FreqRead PPwrite FreqWrite] = prepareWikiPrimaryKeyDist()
% PPread(i,p) is the probability that each instance of trans type 'i' chooses page 'p' for read.
% FreqRead(i,p) is the TPS scaler. When we have 'n' instances of
%   transaction 'i', we consider n*FreqRead(i,p) draws of page 'p' with
%   probability PPread(i,p). In other words, FreqRead(i,p)=A*F where A is the probability that 
%   a trans of type i will NOT ignore page p, and F is the number of rows that
%   if it does NOT ignore page 'p' it will read from it or from other pages
%   that belong to the same table as p.
% Similar for writes.



nTransTypes=5;

%table!
ipblocks=1; 
logging=2; 
page=3; 
page_backup=4; % is not really used by any one! 
page_restrictions=5; 
recent_changes=6; 
revision=7; 
text=8; 
user=9;
user_groups=10;
value_backup=11; % is not really used by any one!
watchlist=12;

tableNames = {'ipblocks','logging','page','page_backup','page_restrictions','recentchanges','revision','text','user','user_groups','value_backup','watchlist'};
nTables = length(tableNames);

rowSize(ipblocks)=84;
rowSize(logging)=123;
rowSize(page)=105;
rowSize(page_backup)=103;
rowSize(page_restrictions)=61;
rowSize(recent_changes)=140;
rowSize(revision)=99;
rowSize(text)=19533;
rowSize(user)=163;
rowSize(user_groups)=39;
rowSize(value_backup)=44;
rowSize(watchlist)=59;

Card = zeros(1, nTables);

Card(ipblocks)=1;
Card(logging)=1503606;
Card(page)=10000;
Card(page_backup)=10000;
Card(page_restrictions)=26;
Card(recent_changes)=1503622;
Card(revision)=1641438;
Card(text)=1641438;
Card(user)=19516;
Card(user_groups)=1927;
Card(value_backup)=2;
Card(watchlist)=41095;


%Pages!
pageSize = 16*1024;
nPages = ceil(Card .* rowSize / pageSize);
totalPages = sum(nPages);
A = cumsum(nPages);
firstPage = [1 A(1:end-1)+1];
lastPage = A;

PPread = zeros(nTransTypes, totalPages);
FreqRead = zeros(nTransTypes, totalPages); %FreqRead(i,p) is the `number of times' that trans type i is going to choose page p with prob PPread(i,p) 
PPwrite = zeros(nTransTypes, totalPages);
FreqWrite = zeros(nTransTypes, totalPages); %FreqRead(i,p) is the `number of times' that trans type i is going to choose page p with prob PPread(i,p)

%%%%%%Transactions!
ADD_WATCHLIST=1;
REMOVE_WATCHLIST=2;
UPDATE_PAGE=3
GET_PAGE_ANONYMOUS=4;
GET_PAGE_AUTHENTICATED=5;

%------------------------------------------------------------------------------------------
%ADD_WATCHLIST
%- insert 1 tuple into watchlist
%- 40.17% chance insert 1 tuple into watchlist (if first was a regular page)
%- update user (Zipfian *1)

PPwrite(ADD_WATCHLIST,firstPage(watchlist):lastPage(watchlist)) = 1.0 / nPages(watchlist); % Warning: INSERT!!
    FreqWrite(ADD_WATCHLIST,firstPage(watchlist):lastPage(watchlist)) = 0.4017;

PPwrite(ADD_WATCHLIST,firstPage(user):lastPage(user)) = ZipfianDist(nPages(user),1);
    FreqWrite(ADD_WATCHLIST,firstPage(user):lastPage(user)) = 1; 

%------------------------------------------------------------------------------------------
%REMOVE_WATCHLIST
%- delete 1 tuple from watchlist
%- 40.17% chance delete 1 tuple into watchlist (if first was a regular page)
%- update user (Zipfian *1)

PPwrite(REMOVE_WATCHLIST,firstPage(watchlist):lastPage(watchlist)) = 1.0 / nPages(watchlist); %Warning: DELETE! 
    FreqWrite(REMOVE_WATCHLIST,firstPage(watchlist):lastPage(watchlist)) = 0.4017;
PPread(REMOVE_WATCHLIST,firstPage(user):lastPage(user)) = ZipfianDist(nPages(user), 1);
    FreqRead(REMOVE_WATCHLIST,firstPage(user):lastPage(user)) = 1;

%------------------------------------------------------------------------------------------
%UPDATE_PAGE
%- insert 1 tuple in table text
%- insert 1 tuple in table revision
%- update 1 tuple in table page (Zipfian 2)
%- insert 1 tuple in table recent_changes
%- read in average 4 tuples from watchlist (the one matching the page selection)? choice of the page is (Zipfian 2), length of the group I don't know (you might infer it from select count(*) as c FROM watchlist group by wl_namespace,wl_title)

%If the above return any tuple: say parm1
%- commit  previous transaction
%- update the same average 4 tuples  from watchlist (if not 
%- commit the watchlist update
%- read average 4 tuples from user (I think similar to Zipfian *1)

%- insert 1 tuple in table logging (this might be part of the initial transaction, or be a third transaction depending on the branch above)
%- update 1 tuple in user (Zipfian *1)
%- update the same 1 tuple in user again

PPwrite(UPDATE_PAGE,firstPage(text):lastPage(text)) = 1.0 / nPages(text); %Warning: INSERT
    FreqWrite(UPDATE_PAGE,firstPage(text):lastPage(text)) = 1;

PPwrite(UPDATE_PAGE,firstPage(revision):lastPage(revision)) = 1.0 / nPages(revision); %Warning: INSERT
    FreqWrite(UPDATE_PAGE,firstPage(revision):lastPage(revision)) = 1;

PPwrite(UPDATE_PAGE,firstPage(page):lastPage(page)) = ZipfianDist(nPages(page), 2);
    FreqWrite(UPDATE_PAGE,firstPage(page):lastPage(page)) = 1;

PPwrite(UPDATE_PAGE,firstPage(recent_changes):lastPage(recent_changes)) = 1.0 / nPages(recent_changes); %Warning: INSERT
    FreqWrite(UPDATE_PAGE,firstPage(recent_changes):lastPage(recent_changes)) = 1;
    
PPread(UPDATE_PAGE,firstPage(watchlist):lastPage(watchlist)) = ZipfianDist(nPages(watchlist), 2);
    FreqRead(UPDATE_PAGE,firstPage(watchlist):lastPage(watchlist)) = 4;
    
parm1 = 0.5;

PPwrite(UPDATE_PAGE,firstPage(watchlist):lastPage(watchlist)) = ZipfianDist(nPages(watchlist), 2);
    FreqWrite(UPDATE_PAGE,firstPage(watchlist):lastPage(watchlist)) = 4*parm1;

PPread(UPDATE_PAGE,firstPage(user):lastPage(user)) = ZipfianDist(nPages(user), 1); %did Carlo mean zipfian 2?
    FreqRead(UPDATE_PAGE,firstPage(user):lastPage(user)) = 4*parm1;
        
PPwrite(UPDATE_PAGE,firstPage(logging):lastPage(logging)) = 1.0 / nPages(logging); %INSERT
    FreqWrite(UPDATE_PAGE,firstPage(logging):lastPage(logging)) = 1;
    
PPwrite(UPDATE_PAGE,firstPage(user):lastPage(user)) = ZipfianDist(nPages(user), 1); %INSERT
    FreqWrite(UPDATE_PAGE,firstPage(user):lastPage(user)) = 1;
    
%------------------------------------------------------------------------------------------
%READ_PAGE
%if the user is logged in: // in the new benchmark we have 2 separate transactions for anonymous or logged-in users? in your version I don't know how frequent this branch is? shouldn't matter much 
%- read 1 tuple from user (Zipfian *1)
%- read 1.04 tuples from user_groups (Zipfian *1)

%In all cases:
%- read 1 tuple from page (Zipfian 2)
%- read 1 tuple from page_restrictions (small table 26 tuples? assume all in one page)
%- read 1 tuple form ipblocks (there is only one tuple so reads that).
%- read 1 tuple form page (same as above, here part of a join) (Zipian 2)
%- read 1 revision tuple (part of the join). The tuple is chosen zipfianly among the current versions? this means that size(revision)-size(page) pages are NEVER accessed, while size(page) tuples in the revision table are accessed zipfianly.
%- read 1 tuple from the text table. As for revision this is zipfian on current tuples, while old version of the article are never accessed.

PPread(GET_PAGE_AUTHENTICATED,firstPage(user):lastPage(user)) = ZipfianDist(nPages(user), 1);
    FreqRead(GET_PAGE_AUTHENTICATED,firstPage(user):lastPage(user)) = 1;

PPread(GET_PAGE_AUTHENTICATED,firstPage(user_groups):lastPage(user_groups)) = ZipfianDist(nPages(user_groups), 1);
    FreqRead(GET_PAGE_AUTHENTICATED,firstPage(user_groups):lastPage(user_groups)) = 1.04;
%%%
PPread(GET_PAGE_AUTHENTICATED,firstPage(page):lastPage(page)) = ZipfianDist(nPages(page), 2);
    FreqRead(GET_PAGE_AUTHENTICATED,firstPage(page):lastPage(page)) = 1;

PPread(GET_PAGE_AUTHENTICATED,firstPage(page_restrictions):lastPage(page_restrictions)) = 1.0 / nPages(page_restrictions);;
    FreqRead(GET_PAGE_AUTHENTICATED,firstPage(page_restrictions):lastPage(page_restrictions)) = 1;

PPread(GET_PAGE_AUTHENTICATED,firstPage(ipblocks):lastPage(ipblocks)) = 1.0 / nPages(ipblocks);;
    FreqRead(GET_PAGE_AUTHENTICATED,firstPage(ipblocks):lastPage(ipblocks)) = 1;

parm2=2;
nCurrentVersions = Card(page);
howManyPages = nCurrentVersions*rowSize(revision) / pageSize;

PPread(GET_PAGE_AUTHENTICATED,firstPage(revision):firstPage(revision)+howManyPages-1) = ZipfianDist(howManyPages, parm2);
PPread(GET_PAGE_AUTHENTICATED,firstPage(revision)+howManyPages:lastPage(revision)) = 0;
    FreqRead(GET_PAGE_AUTHENTICATED,firstPage(revision):lastPage(revision)) = 1;

howManyPages = nCurrentVersions*rowSize(text) / pageSize;
PPread(GET_PAGE_AUTHENTICATED,firstPage(text):firstPage(text)+howManyPages-1) = ZipfianDist(howManyPages, parm2);
PPread(GET_PAGE_AUTHENTICATED,firstPage(text)+howManyPages:lastPage(text)) = 0;
    FreqRead(GET_PAGE_AUTHENTICATED,firstPage(text):lastPage(text)) = 1;

%GET_PAGE_ANONYMOUS    

PPread(GET_PAGE_ANONYMOUS,firstPage(page):lastPage(page)) = ZipfianDist(nPages(page), 2);
    FreqRead(GET_PAGE_ANONYMOUS,firstPage(page):lastPage(page)) = 1;

PPread(GET_PAGE_ANONYMOUS,firstPage(page_restrictions):lastPage(page_restrictions)) = 1.0 / nPages(page_restrictions);;
    FreqRead(GET_PAGE_ANONYMOUS,firstPage(page_restrictions):lastPage(page_restrictions)) = 1;

PPread(GET_PAGE_ANONYMOUS,firstPage(ipblocks):lastPage(ipblocks)) = 1.0 / nPages(ipblocks);;
    FreqRead(GET_PAGE_ANONYMOUS,firstPage(ipblocks):lastPage(ipblocks)) = 1;

howManyPages = nCurrentVersions*rowSize(revision) / pageSize;

PPread(GET_PAGE_ANONYMOUS,firstPage(revision):firstPage(revision)+howManyPages-1) = ZipfianDist(howManyPages, parm2);
PPread(GET_PAGE_ANONYMOUS,firstPage(revision)+howManyPages:lastPage(revision)) = 0;
    FreqRead(GET_PAGE_ANONYMOUS,firstPage(revision):lastPage(revision)) = 1;

howManyPages = nCurrentVersions*rowSize(text) / pageSize;

PPread(GET_PAGE_ANONYMOUS,firstPage(text):firstPage(text)+howManyPages-1) = ZipfianDist(howManyPages, parm2);
PPread(GET_PAGE_ANONYMOUS,firstPage(text)+howManyPages:lastPage(text)) = 0;
    FreqRead(GET_PAGE_ANONYMOUS,firstPage(text):lastPage(text)) = 1;


save('wiki-write.mat','PPwrite', 'FreqWrite');
save('wiki-read.mat','PPread', 'FreqRead');
save('wiki-page-stats.mat','firstPage', 'lastPage', 'Card', 'rowSize');

end

