package CoojaTopologyTester;

public class Node {
    private double[] position;
    private String type;
    
    public Node(double[] position, String type)
    {
        this.position = position;
        this.type = type;
    }


    public double[] getPosition()
    {
        return position;
    }

    public String getType()
    {
        return type;
    }

}
