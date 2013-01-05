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

