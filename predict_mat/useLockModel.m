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

function predictions = useLockModel(conf, counts, workloadName)
 
[tps J f K regions D S0 S g] = prepareLockModel(1, conf, counts(1,:), workloadName);

nRows = size(counts, 1);

all_R =zeros(nRows, J);
all_T_total =zeros(nRows, 1);
all_M_total =zeros(nRows, 1);
all_Vp =zeros(nRows, 9);
all_V =zeros(nRows, 9);
all_W =zeros(nRows, 1);
all_Pcon =zeros(nRows, J);
all_totalWaits =zeros(nRows, J);
all_TimeSpentWaiting =zeros(nRows, J);
all_LocksBeingHeld =zeros(nRows, J);

for row=1:nRows
    [tps J f K regions D S0 S g] = prepareLockModel(1, conf, counts(row,:), workloadName);
    filter = f ./ f;
    filter(isnan(filter))=0;
    
    [R T_total M_total Vp V W U Pcon L] = basicThomasian(tps, J, f, K, regions, D, S0, S, g);
% R_j=latency of tran type J
% T_total = givenT; % the requested TPS
% M_total=total number of transactions in the system
% Vp_i= mean waiting time w.r.t. the active transactions in DBR i
% V_i=blocking time when transactions encounter a lock conflict in DBR i
% W=mean waiting time per lock conflict
% U_jn=mean delay incurred by trans type j when they encounter a lock conflict at step n 
% Pcon_j=prob of lock conflict PER LOCK request for transactions of type j
% L_ji=# of locks held by trans of type j in DBR i

    all_R(row,:)=R .* filter;
    all_T_total(row,:)= T_total;
    all_M_total(row,:) = M_total;
    all_Vp(row,:) = Vp;
    all_V(row,:) = V;
    all_W(row,:) = W;
    all_Pcon(row,:) = Pcon .* filter;
    all_totalWaits(row,:) = Pcon .* K .* f * T_total .* filter;
    all_TimeSpentWaiting(row,:) = Pcon .* K .* f * T_total * W .* filter;
    all_LocksBeingHeld(row, :) = sum((f * T_total) * L, 2);
end

all_totalWaits = dot(all_totalWaits, counts,2);
all_TimeSpentWaiting = dot(all_TimeSpentWaiting, counts,2);
all_LocksBeingHeld = dot(all_LocksBeingHeld, counts,2);

predictions = struct('R', all_R, ...
                     'T_total', all_T_total, ...
                     'M_total', all_M_total, ...  
                     'Vp', all_Vp, ... 
                     'V', all_V, ... 
                     'W', all_W, ... 
                     'Pcon', all_Pcon, ... 
                     'totalWaits', all_totalWaits, ...
                     'TimeSpentWaiting', all_TimeSpentWaiting, ...
                     'LocksBeingHeld', all_LocksBeingHeld);

end

