package manager;

public class SequentialIdGenerator implements IdGenerator{
    private long currentValue;

    public SequentialIdGenerator() {
        this.currentValue = 1;
    }

    @Override
    public long generateId() {
        return currentValue++;
    }
}
