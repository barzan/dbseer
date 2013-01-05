function [diffedM] diff_if_necessary(M)
d = diff(M);

nodiff = [];
diffme = [];
for i=1:size(d,2); 
     if(length(find(d(:,i)<0))>0)
       nodiff = [nodiff i];
     else
       diffme = [diffme i];  
     end
end

diffedM = M;
diffedM(2:end,diffme)= d(:,diffme);

% nodiff
% diffme

end