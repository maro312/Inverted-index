/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.util.ArrayList;

/**
 *
 * @author ehab
 */
 
public class Posting {

    public Posting next = null;
    int docId;
    int dtf = 1;
    public ArrayList<Integer> positions;

    Posting(int id, int t) {
        this.docId = id;
        this.dtf=t;
        this.positions = new ArrayList<Integer>();
    }
    
    Posting(int id) {
        this.docId = id;
        this.positions = new ArrayList<Integer>();
    }
    // Method to add a position to the positions list
    public void addPosition(int position) {
        this.positions.add(position);
        this.dtf++; // Increase term frequency each time a position is added
    }
}