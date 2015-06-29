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

function accesses = carloWikiTransType(transType)

ipblocks_r=1;
ipblocks_w=2;
logging_r=3;
logging_w=4;
page_r=5;
page_w=6;
page_backup_r=7;
page_backup_w=8;
page_restrictions_r=9;
page_restrictions_w=10;
recentchanges_r=11;
recentchanges_w=12;
revision_r=13;
revision_w=14;
text_r=15;
text_w=16;
user_r=17;
user_w=18;
user_groups_r=19;
user_groups_w=20;
value_backup_r=21;
value_backup_w=22;
watchlist_r=23;
watchlist_i=24;
watchlist_d=25;
watchlist_u=26;

  accesses = zeros(1,26);  
    
  if transType>5 || transType<1
      error('Invalid trans type');
  end
  
  if(transType==1)  % AddToWatchlist
   accesses(watchlist_i)=1;
   accesses(user_w)=1;
  end

  if(transType==2)  % getAnonymousREad
   accesses(user_r)=1;
   if(rand(1)>0.9)
     accesses(user_groups_r)=2;
   else
     accesses(user_groups_r)=1;
   end 
   accesses(ipblocks_r)=1;
   accesses(page_r)=2;
   if(rand(1)>0.99)
       accesses(page_restrictions_r)=1;
   end
   accesses(revision_r)=1;
   accesses(text_r)=1;
  end
 
  if(transType==3)  % getLoggedIn reads
      accesses(user_r)=1;
      if(rand(1)>0.9)
         accesses(user_groups_r)=2;
      else
         accesses(user_groups_r)=1;
      end
        accesses(page_r)=2;
      if(rand(1)>0.99)
           accesses(page_restrictions_r)=1;
      end
      accesses(revision_r)=1;
      accesses(text_r)=1;
  end     
       
  if(transType==4)  % RemoveFromWatchlist
   accesses(watchlist_d)=1;
   accesses(user_w)=1;
  end
  
  
  if(transType==5)  % getLoggedIn reads
      accesses(user_r)=1;
      if(rand(1)>0.9)
         accesses(user_groups_r)=2;
      else
         accesses(user_groups_r)=1;
      end
        accesses(page_r)=2;
      if(rand(1)>0.99)
           accesses(page_restrictions_r)=1;
      end
      accesses(revision_r)=1;
      accesses(text_r)=1;

      accesses(text_w)=1;
    accesses(revision_w)=1;
    accesses(page_w)=1;
    accesses(recentchanges_w)=1;
    accesses(watchlist_r)=1;
    accesses(watchlist_u)=1;
    accesses(user_r)=1;
    accesses(logging_w)=1;
    accesses(user_w)=2;  
  end     
  
  
end