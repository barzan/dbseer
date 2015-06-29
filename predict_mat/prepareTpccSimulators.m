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

function [ output_args ] = prepareTpccSimulators( input_args )
% UNTITLED4 Summary of this function goes here
%   Detailed explanation goes here

% this was copied from prepareTpccPrimaryKeyDist.m

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
carlo1 = 1;
carlo2 = 0.5;

Card(customer) = 960000;
Card(district) = 320;
Card(history) = 959423; %bm: 4390440; % specs says 960000
Card(item)=100000; 
Card(new_order)=288430; %bm: 958243;%or 42 or 0? or 32*9000
Card(oorder) = 957267 * carlo1; %bm:11248834; % according to specs this should be 960000!
Card(order_line)=9602210 * carlo2; %bm: 112471350; %or 32*300000;%not sure about this one!
Card(stock)=3200000;
Card(warehouse)=32;

%Pages!
pageSize = 16*1024;
nPages = ceil(Card .* rowSize / pageSize);
totalPages = sum(nPages) + 1;
A = cumsum(nPages);
firstPage = [1 A(1:end-1)+1];
lastPage = A;
NULL = totalPages; % this is the fake page that I use to emulate no page access!

maxSteps = 307;

%stepStarts(1:nTransTypes+1) which states that steps stepStarts(i) to stepStarts(i)-1 belong to trans type i
stepStarts = zeros(1,nTransTypes+1);
%PPPPaccess(s,p) is the probability that the s'th step accesses page p! This
% meams that sum(PPPPaccess(s,:))==1 for all s; same goes for PPPPaccess
PPPaccess = zeros(maxSteps, totalPages);
% accessType(s) is 1 if the s'th step is a read and is 2 if the s'th step is a write 
accessTypes = zeros(1, maxSteps); 
READ = 1;
WRITE = 2;
INSERT = 2;

%%%%%%Transactions!
%type 1: New_Order
stepStarts(1) = 1;
PPPaccess(1,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    accessTypes(1) = READ;
PPPaccess(2,firstPage(warehouse):lastPage(warehouse)) = 1.0 / nPages(warehouse);
    accessTypes(2) = READ;
PPPaccess(3,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
PPPaccess(4,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
    accessTypes(3:4) = WRITE;
PPPaccess(5,firstPage(new_order):lastPage(new_order)-1) = 0.0;
PPPaccess(5,lastPage(new_order)) = 1.0;
    accessTypes(5) = INSERT;
PPPaccess(6,firstPage(oorder):lastPage(oorder)-1) = 0; %insert
PPPaccess(6,lastPage(oorder)) = 1.0; %insert
    accessTypes(6) = INSERT;
PPPaccess(7:16,firstPage(item):lastPage(item)) = 1.0 / nPages(item);
    accessTypes(7:16) = READ;
PPPaccess(17:26,firstPage(stock):lastPage(stock)) = 1.0 / nPages(stock);
    accessTypes(17:26) = READ;
PPPaccess(27:126,firstPage(order_line):lastPage(order_line)-1) = 0;
PPPaccess(27:126,lastPage(order_line)) = 1.0;
    accessTypes(27:126) = WRITE; % inserts
%type 2: Payment
stepStarts(2) = 127;
PPPaccess(127,firstPage(warehouse):lastPage(warehouse)) = 1.0 / nPages(warehouse);
    accessTypes(127) = READ;
PPPaccess(128,firstPage(warehouse):lastPage(warehouse)) = 1.0 / nPages(warehouse);
    accessTypes(128) = WRITE;
PPPaccess(129,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
    accessTypes(129) = READ;
PPPaccess(130,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
    accessTypes(130) = WRITE;
PPPaccess(131:132,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    accessTypes(131:132) = READ;
PPPaccess(133,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    accessTypes(133) = WRITE;
PPPaccess(134,firstPage(history):lastPage(history)-1) = 0; % the trace says table 't' which I guess means 'history'
PPPaccess(134,lastPage(history)) = 1.0; % the trace says table 't' which I guess means 'history'
    accessTypes(134) = INSERT;
%type 3: Order_Status
stepStarts(3) = 135;
PPPaccess(135,firstPage(customer):lastPage(customer)) = 1.0 / nPages(customer);
    accessTypes(135) = READ;
PPPaccess(136,firstPage(oorder):lastPage(oorder)) = 1.0 / nPages(oorder);
    accessTypes(136) = READ;
PPPaccess(137:140,firstPage(order_line):lastPage(order_line)) = 1.0 / nPages(order_line);
    accessTypes(137:140) = READ;
    
%type 4: Delivery
stepStarts(4) = 141;    
% For transaction type 4:
% with probability (659+665) / (659+665+10774) = 0.11:
%    w_new_order_1 -> r_oorder_1 ->  w_oorder_1 -> w_order_line_5 ->  r_order_line_5 -> w_customer_1 -> 
% with prob 1:
%    r_new_order_10 
tempA = 0.11;

PPPaccess(141:150,firstPage(new_order):lastPage(new_order)) = (1-tempA) / nPages(new_order);
PPPaccess(151:154, NULL) = 1-tempA;
    
PPPaccess(141,firstPage(new_order):lastPage(new_order)) = PPPaccess(141,firstPage(new_order):lastPage(new_order)) + tempA / nPages(new_order);
PPPaccess(142,firstPage(oorder):lastPage(oorder)) = PPPaccess(142,firstPage(oorder):lastPage(oorder)) + tempA / nPages(oorder);
PPPaccess(143,firstPage(oorder):lastPage(oorder)) = PPPaccess(143,firstPage(oorder):lastPage(oorder)) + tempA / nPages(oorder);
PPPaccess(144:148,firstPage(order_line):lastPage(order_line)) = PPPaccess(144:148,firstPage(order_line):lastPage(order_line)) + tempA / nPages(order_line);
PPPaccess(149:153,firstPage(order_line):lastPage(order_line)) = PPPaccess(149:153,firstPage(order_line):lastPage(order_line)) + tempA / nPages(order_line);
PPPaccess(154,firstPage(customer):lastPage(customer)) = PPPaccess(154,firstPage(customer):lastPage(customer)) + tempA / nPages(customer);

    accessTypes(141:147) = WRITE; % this is not true
    accessTypes(148:154) = READ; % this is not true

%type 5: Stock_Level
stepStarts(5) = 155;    
PPPaccess(155,firstPage(district):lastPage(district)) = 1.0 / nPages(district);
    accessTypes(155) = READ;
PPPaccess(156:306,firstPage(order_line):lastPage(order_line)) = 1.0 / nPages(order_line);
    accessTypes(156:306) = READ;
PPPaccess(307,firstPage(stock):lastPage(stock)) = 1.0 / nPages(stock);
    accessTypes(307) = READ;    

stepStarts(6) = 308; %just as a marker for end!

save('tpcc-access.mat','PPPaccess', 'stepStarts', 'accessTypes');

end



