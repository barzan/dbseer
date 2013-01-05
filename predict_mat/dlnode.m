classdef dlnode < handle
% DLNODE  A class to represent a doubly-linked list node.
% Multiple dlnode objects may be linked together to create linked lists.
% Each node contains a piece of data and provides access to the next
% and previous nodes.
   properties
      Data
   end
   properties
       belowMiddle
   end
   properties(SetAccess = private)
      Next
      Prev
   end
    
   methods
      function node = dlnode(Data, belowMiddle)
      % DLNODE  Constructs a dlnode object.
         if nargin == 1
            node.Data = Data;
            node.belowMiddle = false;
         else
             if nargin == 2
                node.Data = Data;
                node.belowMiddle = belowMiddle;
             end
         end
      end    
      
      function n = getNext(node)
         n = node.Next; 
      end
      
      function n = getPrev(node)
         n = node.Prev; 
      end
      
      function insertAfter(newNode, nodeBefore)
      % insertAfter  Inserts newNode after nodeBefore.
         disconnect(newNode);
         newNode.Next = nodeBefore.Next;
         newNode.Prev = nodeBefore;
         if ~isempty(nodeBefore.Next)
            nodeBefore.Next.Prev = newNode;
         end
         nodeBefore.Next = newNode;
      end
      
      function insertBefore(newNode, nodeAfter)
      % insertBefore  Inserts newNode before nodeAfter.
         disconnect(newNode);
         newNode.Next = nodeAfter;
         newNode.Prev = nodeAfter.Prev;
         if ~isempty(nodeAfter.Prev)
             nodeAfter.Prev.Next = newNode;
         end
         nodeAfter.Prev = newNode;
      end 

      function disconnect(node)
      % DISCONNECT  Removes a node from a linked list.
      % The node can be reconnected or moved to a different list.
         if ~isscalar(node)
            error('Nodes must be scalar')
         end
         prevNode = node.Prev;
         nextNode = node.Next;
         if ~isempty(prevNode)
             prevNode.Next = nextNode;
         end
         if ~isempty(nextNode)
             nextNode.Prev = prevNode;
         end
         node.Next = [];
         node.Prev = [];
      end
      
      %function delete(node)
      % DELETE  Deletes a dlnode from a linked list.
         %disconnect(node);
      %end  
      
      function disp(node)
      % DISP  Display a link node.
         if (isscalar(node))
            disp('Doubly-linked list node with data:')
            disp(node.Data)
            if node.belowMiddle
                disp('true');
            else
                disp('false');
            end
         else % If node is an object array, display dims only
            dims = size(node);
            ndims = length(dims);
            for k = ndims-1:-1:1
               dimcell{k} = [num2str(dims(k)) 'x'];
            end
            dimstr = [dimcell{:} num2str(dims(ndims))];
            disp([dimstr ' array of doubly-linked list nodes']);
         end
      end 
   end % methods
end % classdef
