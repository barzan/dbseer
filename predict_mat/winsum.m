function x2 = winsum(y,win);

r = size(y,1);
c = size(y,2);

x = y(1:win*floor(r/win),:);

for i=1:c,
   
    t =sum(reshape(x(:,i),win,ceil(prod(size(x(:,i)))/win)))';
    if(i==1),
        x2=t;
    else
        x2 = [x2 t];
    end
end    

end
