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

% Implementing the WOSP Paper

Rtilde_k = Sum_i( Sum_k( A_ik * Rwait_i * (PWread_i*Pread_i + PWwrite_i*Pwrite_i)))

Rwait_i = (N_i-1)*Th_i + L_i

PWread_i = gammaWrite_i * Th_i

Pread_i = gammaRead_i / gamma_i

PWwrite_i = 1 - 1 / (1 + Sum_k^\inf Pi_j=0^(k-1) gamma / mu_{i,(j+1)})

Pwrite_i = gammaWrite_i / gamma_i

mu_ij = 1/Th_i * (Pwrite_i + Pread_i*Sum_k^(j-1) k*(Pread_i)^k + j*(Pread_i)j )

N_i = Sum_j^\inf j*P_j

Th_i = Sum_k (A_ik * D_k) / Sum_k A_ik

L_i = Sum_k (A_ik * D_k^2 * 0.5) / Sum_k (A_ik*D_k)

D_k = Sum_{j=0,j=M} Rhat_j + Sum_{j=k+1,j=M} Rtilde_j + Rcommit

P_j = ?

gammaRead_i = gamma * Sum_k A_ik * (1-W_k)

gammaWrite_i = gamma * Sum_k A_ik * W_k

gamma_i = gammaRead_i + gammaWrite_i

