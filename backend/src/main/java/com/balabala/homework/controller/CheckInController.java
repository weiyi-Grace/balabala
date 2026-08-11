package com.balabala.homework.controller;

import com.balabala.homework.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 打卡控制器 - 使用 Redis 存储打卡记录
 */
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
@Slf4j
public class CheckInController {

    private final StringRedisTemplate redisTemplate;

    private static final String CHECK_IN_KEY_PREFIX = "checkin:";
    private static final String STREAK_KEY_PREFIX = "streak:";
    private static final String FIRST_CHECKIN_KEY_PREFIX = "first_checkin:";

    /**
     * 今日打卡
     */
    @PostMapping("/{studentId}")
    public Result<Map<String, Object>> checkIn(@PathVariable Long studentId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String checkInKey = CHECK_IN_KEY_PREFIX + studentId;

        // 检查今天是否已经打卡
        Boolean alreadyChecked = redisTemplate.opsForSet().isMember(checkInKey, today);
        if (Boolean.TRUE.equals(alreadyChecked)) {
            return Result.success("今日已打卡", getCheckInStats(studentId).getData());
        }

        // 记录打卡
        redisTemplate.opsForSet().add(checkInKey, today);

        // 更新连续打卡天数
        updateStreak(studentId);

        // 记录首次打卡日期（用于计算学习天数）
        String firstCheckInKey = FIRST_CHECKIN_KEY_PREFIX + studentId;
        String firstCheckIn = redisTemplate.opsForValue().get(firstCheckInKey);
        if (firstCheckIn == null) {
            redisTemplate.opsForValue().set(firstCheckInKey, today);
        }

        log.info("学生 {} 打卡成功，日期: {}", studentId, today);
        return Result.success("打卡成功", getCheckInStats(studentId).getData());
    }

    /**
     * 获取打卡统计
     */
    @GetMapping("/{studentId}/stats")
    public Result<Map<String, Object>> getCheckInStats(@PathVariable Long studentId) {
        String checkInKey = CHECK_IN_KEY_PREFIX + studentId;
        String streakKey = STREAK_KEY_PREFIX + studentId;
        String firstCheckInKey = FIRST_CHECKIN_KEY_PREFIX + studentId;

        // 获取所有打卡记录
        Set<String> checkInDates = redisTemplate.opsForSet().members(checkInKey);

        // 计算累计打卡天数
        int totalCheckIns = checkInDates != null ? checkInDates.size() : 0;

        // 获取连续打卡天数
        String streakStr = redisTemplate.opsForValue().get(streakKey);
        int currentStreak = streakStr != null ? Integer.parseInt(streakStr) : 0;

        // 计算学习天数（从首次打卡到今天）
        String firstCheckIn = redisTemplate.opsForValue().get(firstCheckInKey);
        int studyDays = 0;
        if (firstCheckIn != null) {
            LocalDate firstDate = LocalDate.parse(firstCheckIn);
            studyDays = (int) ChronoUnit.DAYS.between(firstDate, LocalDate.now()) + 1;
        }

        // 检查今日是否已打卡
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Boolean checkedInToday = redisTemplate.opsForSet().isMember(checkInKey, today);

        // 获取本月打卡记录
        LocalDate now = LocalDate.now();
        List<String> monthlyCheckIns = new ArrayList<>();
        if (checkInDates != null) {
            for (String date : checkInDates) {
                LocalDate checkDate = LocalDate.parse(date);
                if (checkDate.getMonth() == now.getMonth() && checkDate.getYear() == now.getYear()) {
                    monthlyCheckIns.add(date);
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCheckIns", totalCheckIns);
        stats.put("currentStreak", currentStreak);
        stats.put("studyDays", studyDays);
        stats.put("checkedInToday", Boolean.TRUE.equals(checkedInToday));
        stats.put("monthlyCheckIns", monthlyCheckIns);
        stats.put("checkInDates", new ArrayList<>(checkInDates != null ? checkInDates : new HashSet<>()));

        return Result.success(stats);
    }

    /**
     * 获取打卡日历
     */
    @GetMapping("/{studentId}/calendar")
    public Result<Map<String, Object>> getCheckInCalendar(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam int month) {
        String checkInKey = CHECK_IN_KEY_PREFIX + studentId;
        Set<String> checkInDates = redisTemplate.opsForSet().members(checkInKey);

        List<String> dates = new ArrayList<>();
        if (checkInDates != null) {
            for (String date : checkInDates) {
                LocalDate checkDate = LocalDate.parse(date);
                if (checkDate.getYear() == year && checkDate.getMonthValue() == month) {
                    dates.add(date);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("checkInDates", dates);

        return Result.success(result);
    }

    /**
     * 更新连续打卡天数
     */
    private void updateStreak(Long studentId) {
        String streakKey = STREAK_KEY_PREFIX + studentId;
        String checkInKey = CHECK_IN_KEY_PREFIX + studentId;

        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE);

        // 检查昨天是否打卡
        Boolean checkedYesterday = redisTemplate.opsForSet().isMember(checkInKey, yesterday);

        String currentStreakStr = redisTemplate.opsForValue().get(streakKey);
        int currentStreak = currentStreakStr != null ? Integer.parseInt(currentStreakStr) : 0;

        if (Boolean.TRUE.equals(checkedYesterday)) {
            // 昨天打卡了，连续天数+1
            redisTemplate.opsForValue().set(streakKey, String.valueOf(currentStreak + 1));
        } else {
            // 昨天没打卡，重置为1（今天打卡了）
            redisTemplate.opsForValue().set(streakKey, "1");
        }
    }

    /**
     * 获取近期打卡记录（最近7天）
     */
    @GetMapping("/{studentId}/recent")
    public Result<Map<String, Object>> getRecentCheckIns(@PathVariable Long studentId) {
        String checkInKey = CHECK_IN_KEY_PREFIX + studentId;
        Set<String> checkInDates = redisTemplate.opsForSet().members(checkInKey);

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> recentDays = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_DATE);

            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("date", dateStr);
            dayInfo.put("dayOfWeek", date.getDayOfWeek().getValue());
            dayInfo.put("checkedIn", checkInDates != null && checkInDates.contains(dateStr));
            dayInfo.put("isToday", i == 0);

            recentDays.add(dayInfo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("recentDays", recentDays);

        return Result.success(result);
    }
}
