
package ParkingBuddy.chartPoint;

import ParkingBuddy.dataGetter.ParkingData;
import ParkingBuddy.dataGetter.ParkingStation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChartService {
    public List<DataPoint> getDataPoints() throws IOException {
//        ParkingData data= new ParkingData();
//        LocalDateTime start = LocalDateTime.of(2025, 4, 29, 10, 0);
//        LocalDateTime end = LocalDateTime.of(2025, 4, 30, 23, 0);
//        int code = 103;
//        ParkingStation station = data.getHistoricalData(start, end, code);
//        Map<LocalDateTime, Integer> map = station.getTimestampValueMap();
//
//        List<DataPoint> dataPoints = new ArrayList<DataPoint>();
//        for(LocalDateTime one: map.keySet()){
//            Integer myValue = map.get(one);
//            DataPoint myPoint = new DataPoint(one,myValue);
//            dataPoints.add(myPoint);
//        }
//        System.out.println("We've got data");
//        return dataPoints;
        DataPoint p1 = new DataPoint(323, "p1");
        DataPoint p2 = new DataPoint(400, "p1");
        DataPoint p3 = new DataPoint(300, "p1");
        DataPoint p4 = new DataPoint(320, "p1");
        DataPoint p5 = new DataPoint(350, "p1");

        return List.of(p1, p2, p3, p4, p5);
    }
}

