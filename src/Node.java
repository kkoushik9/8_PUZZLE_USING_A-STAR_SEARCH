public class Node {
    int [][] state = new int[3][3];
    int parentID;
    int fcost;
    int gcost;
    int hcost;
    int stateID;

    public int[][] getState() {
        return state;
    }

    public void setState(int[][] state) {
        this.state = state;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    public int getStateID() {
        return stateID;
    }

    public void setStateID(int stateID) {
        this.stateID = stateID;
    }

    public int getFcost() {
        return fcost;
    }

    public void setFcost(int fcost) {
        this.fcost = fcost;
    }

    public int getGcost() {
        return gcost;
    }

    public void setGcost(int gcost) {
        this.gcost = gcost;
    }

    public int getHcost() {
        return hcost;
    }

    public void setHcost(int hcost) {
        this.hcost = hcost;
    }

}
