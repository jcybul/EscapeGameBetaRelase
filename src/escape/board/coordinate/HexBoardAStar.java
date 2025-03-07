/*******************************************************************************
 * This files was developed for CS4233: Object-Oriented Analysis & Design.
 * The course was taken at Worcester Polytechnic Institute.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright ©2016 Gary F. Pollice
 *******************************************************************************/

package escape.board.coordinate;

import java.util.*;
import escape.board.*;
import escape.board.coordinate.OrthoBoardAStar.Node;
import escape.util.PieceTypeInitializer;
import escape.util.PieceTypeInitializer.PieceAttribute;

/**
 * Description
 * @version 4 may. 2020
 */
public class HexBoardAStar
{
	private final ArrayList<Node> open;
    private final ArrayList<Node> closed;
    private final ArrayList<Node> path;
    private final HexBoard b;
    private Node now;
    private PieceAttribute[] p;
    private final int xstart;
    private final int ystart;
    private int xend, yend;
 
    // Node class for convienience
    static class Node implements Comparable {
        public Node parent;
        public int x, y;
        public double g;
        public double h;
        Node(Node parent, HexCoordinate coord, double g, double h) {
            this.parent = parent;
            this.x = coord.getX();
            this.y = coord.getY();
            this.g = g;
            this.h = h;
       }
       // Compare by f value (g + h)
       @Override
       public int compareTo(Object o) {
           Node that = (Node) o;
           return (int)((this.g + this.h) - (that.g + that.h));
       }
   }
    
    HexBoardAStar(HexBoard b, HexCoordinate coord, PieceAttribute[] p) {
        this.open = new ArrayList<>();
        this.closed = new ArrayList<>();
        this.path = new ArrayList<>();
        this.b = b;
        this.now = new Node(null, coord, 0, 0);
        this.xstart = coord.getX();
        this.ystart = coord.getY();
        this.p = p;
    }
    
    /*
    ** Finds path to xend/yend or returns null
    **
    ** @param (int) xend coordinates of the target position
    ** @param (int) yend
    ** @return (List<Node> | null) the path
    */
    public ArrayList<Node> findPathToHex(int xend, int yend) {
    	
        this.xend = xend;
        this.yend = yend;
        this.closed.add(this.now);
        addNeigborsToOpenList();
        while (this.now.x != this.xend || this.now.y != this.yend) {
            if (this.open.isEmpty()) { // Nothing to examine
                return null;
            }
            this.now = this.open.get(0); // get first node (lowest f score)
            this.open.remove(0); // remove it
            this.closed.add(this.now); // and add to the closed
            addNeigborsToOpenList();
        }
        this.path.add(0, this.now);
        while (this.now.x != this.xstart || this.now.y != this.ystart) {
            this.now = this.now.parent;
            this.path.add(0, this.now);
        }
        if(constectuivePieces(this.path) && !PieceTypeInitializer.canFly(p) ){
        	return null;
        }

        return this.path;
    }
    
    /*
    ** Looks in a given List<> for a node
    **
    ** @return (bool) NeightborInListFound
    */
    private static boolean findNeighborInList(ArrayList<Node> array, Node node) {
        return array.stream().anyMatch((n) -> (n.x == node.x && n.y == node.y));
    }
    
    private void addNeigborsToOpenList() {
        Node node;
        for (int x = -1 ; x < 2; x++) {
            for (int y = -1; y < 2;y++) {

                node = new Node(this.now, HexCoordinate.makeCoordinate(this.now.x + x, this.now.y + y), this.now.g,(double)HexCoordinate.makeCoordinate(now.x, now.y).distanceTo(HexCoordinate.makeCoordinate(x, y)));
                if ((x != 0 || y != 0)
                	&& (x != -1 || y != -1)
                	&& (x != 1 || y != 1)// not this.now 
                	//check the distance is allowed
        			&& 
                    (
                    //check location is not  blocked or can fly or can unlblock
                    b.getLocationType(HexCoordinate.makeCoordinate(this.now.x + x, this.now.y + y)) != LocationType.BLOCK ||
                    PieceTypeInitializer.canUnblock(p) ||
                    PieceTypeInitializer.canFly(p) 
                    )
                  //check location is not exit or is the end
                    &&  (b.getLocationType(HexCoordinate.makeCoordinate(this.now.x + x, this.now.y + y)) != LocationType.EXIT || PieceTypeInitializer.canFly(p)|| HexCoordinate.makeCoordinate(xend, yend).equals(HexCoordinate.makeCoordinate(this.now.x + x, this.now.y + y)))
                  // check next position is empty or it can fly 
                    &&
                    (
                            ((b.getPieceAt(HexCoordinate.makeCoordinate(this.now.x , this.now.y)))  == null || now.x == xstart && now.y == ystart)
                            ||
                            PieceTypeInitializer.canJump(p) && (
                            		(b.getPieceAt(HexCoordinate.makeCoordinate(this.now.x+x,this.now.y+y))) == null && (this.now.parent == null || HexCoordinate.makeCoordinate(this.now.parent.x,this.now.parent.y).sameLine(HexCoordinate.makeCoordinate(node.x, node.y)))
                            		||
                            		(b.getPieceAt(HexCoordinate.makeCoordinate(now.x, now.y)) == null && ( (node.x != xend && node.y != yend) || (HexCoordinate.makeCoordinate(xend, yend).equals(HexCoordinate.makeCoordinate(this.now.x + x, this.now.y + y)) && b.getPieceAt(HexCoordinate.makeCoordinate(xend, yend)).getPlayer() != b.getPieceAt(HexCoordinate.makeCoordinate(xstart, ystart)).getPlayer())))
                            		)	
                            ||
                            // it can fly
                            PieceTypeInitializer.canFly(p) 
                            || ((node.x == xend && node.y == yend) && (b.getPieceAt(HexCoordinate.makeCoordinate(xend, yend)) != null) &&(HexCoordinate.makeCoordinate(xend, yend).equals(HexCoordinate.makeCoordinate(this.now.x + x, this.now.y + y)) && b.getPieceAt(HexCoordinate.makeCoordinate(xend, yend)).getPlayer() != b.getPieceAt(HexCoordinate.makeCoordinate(xstart, ystart)).getPlayer()))
                            )
                    && (b.getLocationType(HexCoordinate.makeCoordinate(this.now.x + x, this.now.y + y)) != LocationType.EXIT || PieceTypeInitializer.canFly(p) || HexCoordinate.makeCoordinate(xend, yend).equals(HexCoordinate.makeCoordinate(this.now.x + x, this.now.y + y)))
                    && !findNeighborInList(this.open, node) && !findNeighborInList(this.closed, node)) { // if not already done
                        node.g = node.parent.g + 1; // Horizontal/vertical cost = 1.0
                        node.g += (double)HexCoordinate.makeCoordinate(now.x, now.y).distanceTo(HexCoordinate.makeCoordinate(x, y));
                     // add movement cost for this square

                        this.open.add(node);
                }
            
            }
        }
        Collections.sort(this.open);
    }
    
    /**
     * check for consecutive pieces or if it is the last two and it can jump and the
     * last piece is an enemy then it can jump and kill
     * @param arr
     * @return
     */
    private boolean constectuivePieces(ArrayList<Node> arr) {
    	for(int i = 0;i < arr.size()-2;i++) {
    		if(b.getPieceAt(HexCoordinate.makeCoordinate(arr.get(i).x,arr.get(i).y)) != null 
    				&& b.getPieceAt(HexCoordinate.makeCoordinate(arr.get(i+1).x,arr.get(i+1).y)) != null
    				&& b.getPieceAt(HexCoordinate.makeCoordinate(arr.get(i+2).x,arr.get(i+2).y))!= null){
    			
    			if(b.getPieceAt(HexCoordinate.makeCoordinate(arr.get(arr.size()-2).x,arr.get(path.size()-2).y)) != null
    			        	&&b.getPieceAt(HexCoordinate.makeCoordinate(arr.get(arr.size()-1).x,arr.get(arr.size()-1).y)) != null
    			        	&& b.getPieceAt(HexCoordinate.makeCoordinate(arr.get(arr.size()-1).x,arr.get(arr.size()-1).y)).getPlayer() !=
    			        			b.getPieceAt(HexCoordinate.makeCoordinate(xstart,ystart)).getPlayer()) {
    				return false;
    			}
    			return true;
    					
    				}
    	}
    	return false;
	
    }

}

