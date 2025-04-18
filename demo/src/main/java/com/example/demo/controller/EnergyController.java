package com.example.demo.controller;

import com.example.demo.dto.EnergyData;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    @GetMapping("/current")
    public List<EnergyData> getCurrentData() {
        List<EnergyData> data = generateData();
        List<EnergyData> currentData = new ArrayList<>();
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.HOUR, -1);
        Date oneHourAgo = cal.getTime();

        for (EnergyData d : data) {
            if (!d.getHour().before(oneHourAgo) && !d.getHour().after(now)) {
                currentData.add(d);
            }
        }

        return currentData;
    }

    @GetMapping("/historical")
    public List<EnergyData> getHistoricalData(@RequestParam("dateStart") Date dateStart,
                                              @RequestParam("dateEnd") Date dateEnd) {
        List<EnergyData> data = generateData();
        List<EnergyData> result = new ArrayList<>();

        for (EnergyData d : data) {
            if (!d.getHour().before(dateStart) && !d.getHour().after(dateEnd)) {
                result.add(d);
            }
        }
        return result;
    }

    private List<EnergyData> generateData() {
        List<EnergyData> dataList = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            dataList.add(new EnergyData(sdf.parse("2025-04-09T12:00:00"), 18.5, 17.9, 1.1));
            dataList.add(new EnergyData(sdf.parse("2025-04-09T13:00:00"), 20.0, 21.2, 2.0));
            dataList.add(new EnergyData(sdf.parse("2025-04-09T14:00:00"), 22.4, 22.4, 0.0));
            dataList.add(new EnergyData(sdf.parse("2025-04-09T15:00:00"), 24.1, 23.7, 1.8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }
}
