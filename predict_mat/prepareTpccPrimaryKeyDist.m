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

function [PPread FreqRead PPwrite FreqWrite] = prepareTpccPrimaryKeyDist()
% PPread(i,p) is the probability that each instance of trans type 'i' chooses page 'p' for read.
% FreqRead(i,p) is the TPS scalaer. When we have 'n' instances of
%   transaction 'i', we consider n*FreqRead(i,p) draws of page 'p' with
%   probability PPread(i,p). In other words, FreqRead(i,p)=A*F where A is the probability that 
%   a trans of type i will ignore page p, and F is the number of rows that
%   if it does NOT ignore page 'p' it will read from it or from other pages
%   that belong to the same table as p.
% Similar for writes.



nTransTypes=5;

%table!
customer=1; 
district=2; 
history=3; 
item=4; 
new_order=5; 
oorder=6; 
order_line=7; 
stock=8; 
warehouse=9;

tableNames = {'customer','district','history','item','new_order','oorder','order_line','stock','warehouse'};
nTables = length(tableNames);

rowSize = zeros(1, nTables);
rowSize(customer)=614;
rowSize(district)=208;
rowSize(history)=82;
rowSize(item)=99;
rowSize(new_order)=40;
rowSize(oorder)=102;
rowSize(order_line)=80;
rowSize(stock)=348;
rowSize(warehouse)=512;

Card = zeros(1, nTables);

Card(customer) = 960000; 
Card(district) = 320; 
Card(history) = 4390440; % specs says 960000
Card(item)=100000; 
Card(new_order)=958243;%or 42 or 0? or 32*9000
Card(oorder) = 11248834; % according to specs this should be 960000!
Card(order_line)=112471350; %or 32*300000;%not sure about this one!
Card(stock)=3200000; 
Card(warehouse)=32;

%Pages!
pageSize = 16*1024;
nPages = ceil(Card .* rowSize / pageSize);
totalPages = sum(nPages);
A = cumsum(nPages);
firstPage = [1 A(1:end-1)];
lastPage = A;

PPread = zeros(nTransTypes, totalPages);
FreqRead = zeros(nTransTypes, totalPages); %FreqRead(i,p) is the `number of times' that trans type i is going to choose page p with prob PPread(i,p) 
PPwrite = zeros(nTransTypes, totalPages);
FreqWrite = zeros(nTransTypes, totalPages); %FreqRead(i,p) is the `number of times' that trans type i is going to choose page p with prob PPread(i,p)

%%%%%%Transactions!

PPread(1,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    FreqRead(1,firstPage(customer):lastPage(customer)) = 1;
PPread(1,firstPage(warehouse):lastPage(warehouse)) = 1.0 / nPages(warehouse);
    FreqRead(1,firstPage(warehouse):lastPage(warehouse)) = 1;

PPwrite(1,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
    FreqWrite(1,firstPage(district):lastPage(district)) = 2;

PPwrite(1,firstPage(new_order):lastPage(new_order)) = 1.0 / nPages(new_order);
    FreqWrite(1,firstPage(new_order):lastPage(new_order)) = 1; % insert

PPwrite(1,firstPage(oorder):lastPage(oorder)) = 1.0 / nPages(oorder);
    FreqWrite(1,firstPage(oorder):lastPage(oorder)) = 1; %insert

PPread(1,firstPage(item):lastPage(item)) = 1.0 / nPages(item);
    FreqRead(1,firstPage(item):lastPage(item)) = 10;

PPread(1,firstPage(stock):lastPage(stock)) = 1.0 / nPages(stock);
    FreqRead(1,firstPage(stock):lastPage(stock)) = 10; % I only count them when they get the locks with "select for update"

PPwrite(1,firstPage(order_line):lastPage(order_line)) = 1.0 / nPages(order_line);
    FreqWrite(1,firstPage(order_line):lastPage(order_line)) = 100; % inserts

PPread(2,firstPage(warehouse):lastPage(warehouse)) = 1.0 / nPages(warehouse);
    FreqRead(2,firstPage(warehouse):lastPage(warehouse)) = 1;
PPwrite(2,firstPage(warehouse):lastPage(warehouse)) = 1.0 / nPages(warehouse);
    FreqWrite(2,firstPage(warehouse):lastPage(warehouse)) = 1;
PPread(2,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
    FreqRead(2,firstPage(district):lastPage(district)) = 1;
PPwrite(2,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
    FreqWrite(2,firstPage(district):lastPage(district)) = 1;
PPread(2,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    FreqRead(2,firstPage(customer):lastPage(customer)) = 2;
PPwrite(2,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    FreqWrite(2,firstPage(customer):lastPage(customer)) = 1;
PPwrite(2,firstPage(history):lastPage(history)) = 1.0 / nPages(history); % the trace says table 't' which I guess means 'history'
    FreqWrite(2,firstPage(history):lastPage(history)) = 1; % insert    
    
PPread(3,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    FreqRead(3,firstPage(customer):lastPage(customer)) = 1;
PPread(3,firstPage(oorder):lastPage(oorder)) = 1.0 / nPages(oorder);
    FreqRead(3,firstPage(oorder):lastPage(oorder)) = 1;
PPread(3,firstPage(order_line):lastPage(order_line)) = 1.0 / nPages(order_line);
    FreqRead(3,firstPage(order_line):lastPage(order_line)) = 4;
    
% For transaction type 4:
% with probability (659+665) / (659+665+10774) = 0.11:
%    w_new_order_1 -> r_oorder_1 ->  w_oorder_1 -> w_order_line_5 ->  r_order_line_5 -> w_customer_1 -> 
% with prob 1:
%    r_new_order_10 
tempA = 0.11;

PPread(4,firstPage(new_order):lastPage(new_order)) = 1.0 / nPages(new_order);
    FreqRead(4,firstPage(new_order):lastPage(new_order)) = 10;
PPwrite(4,firstPage(new_order):lastPage(new_order)) = 1.0 / nPages(new_order);
    FreqWrite(4,firstPage(new_order):lastPage(new_order))=1 * tempA;
PPread(4,firstPage(oorder):lastPage(oorder)) = 1.0 / nPages(oorder);
    FreqRead(4,firstPage(oorder):lastPage(oorder))=1 * tempA;
PPwrite(4,firstPage(oorder):lastPage(oorder)) = 1.0 / nPages(oorder);    
    FreqWrite(4,firstPage(oorder):lastPage(oorder))=1 * tempA;
PPwrite(4,firstPage(order_line):lastPage(order_line)) = 1.0 / nPages(order_line);
    FreqWrite(4,firstPage(order_line):lastPage(order_line))=5 * tempA;
PPread(4,firstPage(order_line):lastPage(order_line)) = 1.0 / nPages(order_line);
    FreqRead(4,firstPage(order_line):lastPage(order_line))=5 * tempA;
PPwrite(4,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    FreqWrite(4,firstPage(customer):lastPage(customer))=1 * tempA;
    
PPread(5,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
    FreqRead(5,firstPage(district):lastPage(district)) = 1;
PPread(5,firstPage(order_line):lastPage(order_line)) = 1.0 / nPages(order_line);
    FreqRead(5,firstPage(order_line):lastPage(order_line)) = 151;
PPread(5,firstPage(stock):lastPage(stock)) = 1.0 / nPages(stock);
    FreqRead(5,firstPage(stock):lastPage(stock)) = 1;    


save('tpcc-write.mat','PPwrite', 'FreqWrite');
save('tpcc-read.mat','PPread', 'FreqRead');
save('tpcc-page-stats.mat','firstPage', 'lastPage', 'Card', 'rowSize');

end

