package sanitizr.project.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sanitizr.project.entity.DemandEntity;
import sanitizr.project.service.DemandService;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@RestController
@RequestMapping("/demand")
public class DemandController {
    private final DemandService demandService;
    private final int[] TIME_PERIODS = new int[]{8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
    private final String[] TOILET_IDS = new String[]{"A0F", "A0M", "M0M", "M0F", "S0F", "S0M", "C0F", "C0M", "B0F", "D0M", "D0F", "D1F", "D1M", "D2M"};

    DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @GetMapping("/all")
    public List<DemandEntity> getDemands() {
        return demandService.getAll();
    }

    @GetMapping("/recent_week")
    public List<DemandEntity> getRecentWeekDemands() {
        LocalDate today = Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // 获取上周一 00:00:00 和上周五 23:59:59 的 Date 对象
        Date lastMondayStart = Date.from(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());

        Date lastFridayEnd = Date.from(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant());
        return demandService.getAllByTimeBetween(lastMondayStart, lastFridayEnd);
    }

    @GetMapping("/prediction")
    public Map<String, HashMap<Integer, Integer>> getPrediction() {
        LocalDate time = Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        Integer day = time.getDayOfWeek().getValue();
        // 获取上周一 00:00:00 和上周五 23:59:59 的 Date 对象
        Date lastMondayStart = Date.from(time.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());

        Date lastFridayEnd = Date.from(time.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant());
        System.out.println(lastFridayEnd);
        Map<String, HashMap<Integer, Integer>> frequencies = new HashMap<>();
        for (String toiletId : TOILET_IDS) {
            frequencies.putIfAbsent(toiletId, new HashMap<>());
        }
        for (int period : TIME_PERIODS) {
            for (String toiletId : TOILET_IDS) {
                int totalFrequency = (int) Math.ceil(demandService.countAllByDayAndPeriodAndToiletId(day, period, toiletId) * 0.5);
                int recentWeekFrequency = (int) Math.ceil(demandService.countAllByDayAndPeriodAndTimeBetweenAndToiletId(day, period, lastMondayStart, lastFridayEnd, toiletId) * 0.5);
                int weightedFrequency = totalFrequency + recentWeekFrequency;
                HashMap<Integer, Integer> currentToiletFrequencies = frequencies.get(toiletId);
                currentToiletFrequencies.put(period, weightedFrequency);
                frequencies.put(toiletId, currentToiletFrequencies);
            }
        }
        return frequencies;
    }

    @PostMapping("/")
    public void addDemand(@RequestBody RequestData requestData) {
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;  // 1=周日，转为 1=周一
        if (dayOfWeek == 0) dayOfWeek = 7;  // 将周日设置为 7

        // 获取当前时间段（小时部分舍入）
        int period = calendar.get(Calendar.HOUR_OF_DAY);
        demandService.addDemand(date, dayOfWeek, period, requestData.getToiletId());
    }

    @GetMapping("/frequencies/all")
    public List<Map<String, Object>> getAllFrequencies() {
        List<Map<String, Object>> frequenciesList = new ArrayList<>();

        for (String toiletId : TOILET_IDS) {
            Map<Integer, Integer> frequencyMap = new HashMap<>();

            for (int period : TIME_PERIODS) {
                int totalFrequency = (int) Math.ceil(demandService.countAllByPeriodAndToiletId(period, toiletId));
                frequencyMap.put(period, totalFrequency);
            }

            // 将当前 toiletId 的数据加入到 List
            Map<String, Object> frequencyData = new HashMap<>();
            frequencyData.put("toiletId", toiletId);
            frequencyData.put("frequencies", frequencyMap);

            frequenciesList.add(frequencyData);
        }

        return frequenciesList;
    }

    @GetMapping("/frequencies/today")
    public List<Map<String, Object>> getAllTodayFrequencies() {
        LocalDate time = Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        Date today = Date.from(time.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Map<String, Object>> frequenciesList = new ArrayList<>();

        for (String toiletId : TOILET_IDS) {
            Map<Integer, Integer> frequencyMap = new HashMap<>();

            for (int period : TIME_PERIODS) {
                int totalFrequency = (int) Math.ceil(demandService.countAllByTimeAfterAndPeriodAndToiletId(today, period, toiletId));
                frequencyMap.put(period, totalFrequency);
            }

            // 将当前 toiletId 的数据加入到 List
            Map<String, Object> frequencyData = new HashMap<>();
            frequencyData.put("toiletId", toiletId);
            frequencyData.put("frequencies", frequencyMap);

            frequenciesList.add(frequencyData);
        }

        return frequenciesList;
    }

    @GetMapping("/frequencies/now")
    public List<Map<String, Object>> getAllNowFrequencies() {
        LocalTime localTime = Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        Date date = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
        int period = localTime.getHour();
        List<Map<String, Object>> frequenciesList = new ArrayList<>();

        for (String toiletId : TOILET_IDS) {
            int totalFrequency = (int) Math.ceil(demandService.countAllByTimeAfterAndPeriodAndToiletId(date, period, toiletId));
            // 将当前 toiletId 的数据加入到 List
            Map<String, Object> frequencyData = new HashMap<>();
            frequencyData.put("toiletId", toiletId);
            frequencyData.put("frequency", totalFrequency);
            frequenciesList.add(frequencyData);
        }
        return frequenciesList;
    }

    @GetMapping("/prediction/today/period")
    public ArrayList<Map<String, Object>> getPredictionByPeriod(
            @RequestParam(value = "period") Integer period
    ) {
        LocalDate time = Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        Integer day = time.getDayOfWeek().getValue();
        // 获取上周一 00:00:00 和上周五 23:59:59 的 Date 对象
        Date lastMondayStart = Date.from(time.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());

        Date lastFridayEnd = Date.from(time.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant());
        System.out.println(lastFridayEnd);
        ArrayList<Map<String,Object>> frequencies = new ArrayList<>();
        for (String toiletId : TOILET_IDS) {
            int totalFrequency = (int) Math.ceil(demandService.countAllByDayAndPeriodAndToiletId(day, period, toiletId) * 0.5);
            int recentWeekFrequency = (int) Math.ceil(demandService.countAllByDayAndPeriodAndTimeBetweenAndToiletId(day, period, lastMondayStart, lastFridayEnd, toiletId) * 0.5);
            int weightedFrequency = totalFrequency + recentWeekFrequency;
            Map<String, Object> frequency = new HashMap<>();
            frequency.put("toiletId", toiletId);
            frequency.put("frequency", weightedFrequency);
            frequencies.add(frequency);
        }
        return frequencies;
    }
}

