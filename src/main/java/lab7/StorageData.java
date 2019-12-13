package lab7;

public class StorageData {
    private int left;
    private int right;
    private long time;

    public StorageData(int left, int right, long time){
        this.left = left;
        this.right = right;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public int getLeft(){
        return left;
    }

    public int getRight() {
        return right;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
