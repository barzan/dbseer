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

function [tps_ J_ f_ K_ regions_ D_ S0_ S_ g_] = prepareLockModel(initialize, conf, counts, workloadName)

persistent oldConfig oldWorkloadName;
persistent J K regions  D  S  g readRows updatedRows;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
if strcmp(workloadName, 'LOCK1')
    beginCost = conf(1);
    commitCost = conf(2);
elseif strcmp(workloadName, 'TPCC')
    beginCost = conf(1);
    interLockInterval = conf(2);
    DomainMultiplier =  conf(3); %0.0000000100;
    costMultiplier= conf(4);
else
    error(['Unknown workloadName: ' workloadName]);
end
nano = 1000000000.0;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if initialize==1 || ~isequal(conf, oldConfig) || ~strcmp(workloadName, oldWorkloadName)
    oldConfig = conf;
    oldWorkloadName = workloadName;
    if strcmp(workloadName, 'LOCK1')
        J = 1; % # of transaction classes, e.g. in TPC-C, J=5
        f = [1.0]; % frequency matrix for each class
        K = [2]; % the number of locks requested in each transaction class
        regions = 1; % number of database regions, perhaps should be equal to the number of tables?!
        D = [1024]; % number of data items in the i'th DB region
        maxK = max(K);
        S = zeros(J,maxK); % S_jn is the processing of the n'th step of a transaction of class C_j
        S0 = beginCost; % Barzan assumes that everything that precedes the first lock is constant time for all transactions
        S(1,1) = 0.001;
        S(1,2) = 0.001 + commitCost;

        g = zeros([J maxK regions], 'double');
        g(1,:,1) = 1.0;
    elseif strcmp(workloadName, 'TPCC')
        J = 5;
        f = [0.2 0.2 0.2 0.2 0.2];
        K = [136 8 6 10 153]; % info for transaction type 4 is not accurate!
        regions = 9;
        customer=1; district=2; history=3; item=4; new_order=5; oorder=6; order_line=7; stock=8; warehouse=9;
        D = zeros(1, regions);
        D(customer) = 960000; D(district) = 320; D(history) = 4390440; % specs says 960000
        D(item)=100000; D(new_order)=42;%or 0? or 32*9000
        D(oorder) = 5255647; % according to specs this should be 960000!
        D(order_line)=32*300000;%not sure about this one!
        D(stock)=3200000; D(warehouse)=32;

        maxK = max(K);
        S = zeros(J,maxK, 'double');
        S0 = beginCost + dot([2096  0.1*6403+0.9*3608 1753 1543 4170], f) / nano;
        S(1,1:136) = [interLockInterval 727780 609558 564452 604045 666491 574574 736047 568746 730061 563094 722544 563341 715844 565713 734241 562731 ...
            727079 570391 727549 562944 723694 569319 720982 571238 729174 repmat(interLockInterval, 1, 99) 2305666 repmat(interLockInterval, 1, 9) 5241200];
        S(2,1:8) = 0.1*[589150 623830 590536 644205 946331 647169 797044 3018040] ...
                  +0.9*[586701 618534 588888 643966 950617 0 700665 3515563];
        S(3,1:6) = [952166 77273 repmat(interLockInterval, 1, 3) 7834434];
        S(4,1:10) = [640781 626863 621894 617058 617356 615745 613847 614119 615625 3111715];
        S(5,1:153) = [591071 repmat(interLockInterval, 1, 151) 8500096];

        S0 = beginCost + (S0-beginCost) * costMultiplier;
        S = (S/nano) * costMultiplier;

        readRows = zeros(J, regions); %b_ji= the probability that
        updatedRows = zeros(J, regions);

        g = zeros([J maxK regions], 'double');
        g(1,1,customer) = 1.0;
        g(1,2,warehouse) = 1.0;
        g(1,3,district) = 1.0;
        g(1,4,new_order) = 1.0;
        g(1,5,district) = 1.0;
        g(1,6,oorder) = 1.0;
        g(1,[7  9 11 13 15 17 19 21 23 25],item) = 1.0;
        g(1,[8 10 12 14 16 18 20 22 24 26],stock) = 1.0;
        g(1,27:126,order_line) = 1.0;
        g(1,127:136,stock) = 1.0;
            readRows(1,customer) = 1;
            readRows(1,warehouse) = 1;
            updatedRows(1,district) = 2;
            readRows(1,new_order) = 1; % insert
            readRows(1,oorder) = 1; %insert
            readRows(1,item) = 10;
            updatedRows(1,stock) = 10; % I only count them when they get the locks with "select for update"
            readRows(1,order_line) = 100; % inserts
        g(2,1,warehouse) = 1.0;
        g(2,2,warehouse) = 1.0;
        g(2,3,district) = 1.0;
        g(2,4,district) = 1.0;
        g(2,5,customer) = 1.0;
        g(2,6,customer) = 1.0;
        g(2,7,customer) = 1.0;
        g(2,8,history) = 1.0; % the trace says table 't' which I guess means 'history'
            readRows(2,warehouse) = 1;
            updatedRows(2,warehouse) = 1;
            readRows(2,district) = 1;
            updatedRows(2,district) = 1;
            readRows(2,customer) = 2;
            updatedRows(2,customer) = 1;
            readRows(2,history) = 1; % insert
        g(3,1,customer) = 1.0;
        g(3,2,oorder) = 1.0;
        g(3,3:6,order_line) = 1.0;
            readRows(3,customer) = 1;
            readRows(3,oorder) = 1;
            readRows(3,order_line) = 4;

% For transaction type 4:
% with probability (659+665) / (659+665+10774) = 0.11:
%    w_new_order_1 -> r_oorder_1 ->  w_oorder_1 -> w_order_line_5 ->  r_order_line_5 -> w_customer_1 ->
% with prob 1:
%    r_new_order_10
tempA = 0.11;
pBranch = 1-(1-tempA)^0.1;
        g(4,1:10,new_order) = 1.0 - 3*pBranch;
            readRows(4,new_order) = 10; % I have to yse number of rows as a probability instead of a simple count! this way I can also account for other types of transaction 4
            updatedRows(4, new_order) = tempA/(1.0 - 3*pBranch); % to make sure in the end we update the new_order w prob 0.11 too!
        g(4,1:10,oorder) = pBranch; % this way the overall prob that tran type 4 accesses oorder adds up to tempA !
            readRows(4,oorder) = 1;
            updatedRows(4,oorder) = 1;
        g(4,1:10,order_line) = pBranch;
            updatedRows(4,order_line) = 5;
            readRows(4,order_line) = 5;
        g(4,1:10,customer) = pBranch;
            updatedRows(4,customer) = 1;

            %w_new_order_1 w_oorder_1 w_order_line_5 w_customer_1

        g(5,1,district) = 1.0;
        g(5,2:152,order_line) = 1.0;
        g(5,153,stock) = 1.0;
            readRows(5,district) = 1;
            readRows(5,order_line) = 151;
            readRows(5,stock) = 1;

            S0 = beginCost + (dot([2096  0.1*6403+0.9*3608 1753 1543 4170], f) / nano) * costMultiplier;

    else
        error(['Unknown workloadName: ' workloadName]);
    end
end % end of initialization
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% This is the main part!

f = counts;
tps = sum(f,2);
f = f / tps;

b = zeros(1, regions);
for i=1:regions
  sm = (readRows(:,i)+updatedRows(:,i));
  sm(sm==0) = 123456;
  b(i) = dot(f, (updatedRows(:,i) ./ sm)');
end
b(b==0)=0.0000000100; %DomainMultiplier;
D = D ./ (1- (1-b.*b)); % when we do have exclusive locks we should not shrink all the tables!

D = D * DomainMultiplier;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
   tps_ = tps;
   J_ = J;
   f_ = f;
   K_ = K;
   regions_ = regions;
   D_ = D;
   S0_ = S0;
   S_ = S;
   g_ = g;
end
