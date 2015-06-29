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

function mainPrediction = fitRealistic(conf, data)
ERROR you should not be calling this function anymore!
newConf = [0.125 0.0001 conf(1) conf(2)];

[tps J f K regions D S0 S g] = prepareLockModel(1, newConf,data(1,:) );


AllLatencies = zeros(size(data,1), J);
AggLockMetrics = zeros(size(data,1), 3);

for row=1:size(data,1)   
   [tps J f K regions D S0 S g] = prepareLockModel(0, newConf,data(row,:) );
   
   [R T_total M_total Vp V W U Pcon L] = realisticBarzan(tps, J, f, K, regions, D, S0, S, g);
   filter = f ./ f;
   filter(isnan(filter))=0;
   AllLatencies(row,:) = R .* filter;
   AggLockMetrics(row,1) = -1; %'locks being waited for' I don't know how to calculate
   AggLockMetrics(row,2) = sum(Pcon .* K .* f * T_total); %waits due to locks
   AggLockMetrics(row,3) = sum(Pcon .* K .* f * T_total) * W; %time spent waiting for locks
   
end

%Uncomment for fitting the latencies
%mainPrediction = AllLatencies(:,:);
%Uncomment for fitting the total wait time
mainPrediction = AggLockMetrics(:,3);

%Uncomment if returned Inf or NaN during training!
mainPrediction(isnan(mainPrediction)) = 1e+123;
mainPrediction(mainPrediction==Inf)= 1e+124;



%New Order:
%1 [2085]: (2096) r_customer_1 -> (-1) r_warehouse_1 -> (727780) w_district_1 -> (609558) w_new_order_1 -> (564452) w_district_1 -> (604045) w_oorder_1 -> (666491) r_item_1 -> (574574) w_stock_1 -> (736047) r_item_1 -> (568746) w_stock_1 -> (730061) r_item_1 -> (563094) w_stock_1 -> (722544) r_item_1 -> (563341) w_stock_1 -> (715844) r_item_1 -> (565713) w_stock_1 -> (734241) r_item_1 -> (562731) w_stock_1 -> (727079) r_item_1 -> (570391) w_stock_1 -> (727549) r_item_1 -> (562944) w_stock_1 -> (723694) r_item_1 -> (569319) w_stock_1 -> (720982) r_item_1 -> (571238) w_stock_1 -> (729174) w_order_line_100 -> (2305666) w_stock_10 (5241200)

%Payment: 10% and 90%
%2 [2258]: (6403) w_warehouse_1 -> (589150) r_warehouse_1 -> (623830) w_district_1 -> (590536) r_district_1 -> (644205) r_customer_1 -> (946331) r_customer_1 -> (647169) w_customer_1 -> (797044) w_t_1 (3018040)
%2 [21175]: (3608) w_warehouse_1 -> (586701) r_warehouse_1 -> (618534) w_district_1 -> (588888) r_district_1 -> (643966) r_customer_1 -> (950617) w_customer_1 -> (700665) w_t_1 (3515563)

%Order status:
%3 [23389]: (1753) r_customer_1 -> (952166) r_oorder_1 -> (772731) r_order_line_4 (7834434)

%Stock Level:
%5 [23688]: (4170) r_district_1 -> (591071) r_order_line_151 -> (-1) r_stock_1 (8500096)

%=======
%Delivery:
%4: (X | XY )^10 where
%X=r_new_order_1 and
%Y=w_new_order_1 -> (597737) r_oorder_1 -> (604979) w_oorder_1 -> (669883) w_order_line_5 -> (957606) r_order_line_5 -> (711980) w_customer_1
%examples:

%4 [659]: (1711) r_new_order_1 -> (645559) r_new_order_1 -> (627750) r_new_order_1 -> (618009) r_new_order_1 -> (614827) r_new_order_1 -> (615823) r_new_order_1 -> (622614) w_new_order_1 -> (597737) r_oorder_1 -> (604979) w_oorder_1 -> (669883) w_order_line_5 -> (957606) r_order_line_5 -> (711980) w_customer_1 -> (673967) r_new_order_1 -> (631961) r_new_order_1 -> (619612) r_new_order_1 -> (628410) r_new_order_1 (3064832)

%4 [665]: (1866) r_new_order_1 -> (654711) r_new_order_1 -> (628988) r_new_order_1 -> (627286) r_new_order_1 -> (617150) r_new_order_1 -> (621713) r_new_order_1 -> (619522) r_new_order_1 -> (621146) r_new_order_1 -> (624339) r_new_order_1 -> (629056) w_new_order_1 -> (606380) r_oorder_1 -> (620615) w_oorder_1 -> (677747) w_order_line_4 -> (962373) r_order_line_5 -> (725088) w_customer_1 -> (667880) r_new_order_1 (1820328)

%4 [10774]: (1543) r_new_order_1 -> (640781) r_new_order_1 -> (626863) r_new_order_1 -> (621894) r_new_order_1 -> (617058) r_new_order_1 -> (617356) r_new_order_1 -> (615745) r_new_order_1 -> (613847) r_new_order_1 -> (614119) r_new_order_1 -> (615625) r_new_order_1 (3111715)




end






