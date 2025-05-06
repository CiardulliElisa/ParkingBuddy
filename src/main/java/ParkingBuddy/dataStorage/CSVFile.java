public class CSVFile implements ReadData, SaveData{

    @Override
    public Object[] readData(String filepath) {
        return new Object[0];
    }

    @Override
    public boolean saveData(Object[] data, String filepath) {
        return false;
    }
}
