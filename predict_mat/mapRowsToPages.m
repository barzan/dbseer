function P = mapRowsToPages(domain_cardinality, totalRowsChanged)
%This function returns the number of unique pages touched (i.e., dirtied) when the total
%number of requests is `totalRowsChanged' and the total number of unique
%pages is `domain_cardinality'

epsilon = 1e-10;

    n = floor(totalRowsChanged);
    fractionN = totalRowsChanged - n;
    
    D = floor(domain_cardinality);
    fractionD = domain_cardinality - D;

    if fractionN>epsilon
        P = fractionN * mapRowsToPages(domain_cardinality, n+1) + (1-fractionN)*mapRowsToPages(domain_cardinality, n);
    else
       if fractionD>epsilon
           P = fractionD * mapRowsToPages(D+1, n) + (1-fractionD)*mapRowsToPages(D, n);
       else
           if D==0
                P = 0;
           else
                P=  D - D * (1-1/D).^n;
           end
       end
    end
end

