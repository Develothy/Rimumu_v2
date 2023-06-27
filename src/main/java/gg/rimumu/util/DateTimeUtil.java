package gg.rimumu.util;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class DateTimeUtil {

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    public static final String yyyyMMddHHmm = "yyyy-MM-DD HH:mm";
    private static ZoneId KST = ZoneId.of("Asia/Seoul");

    public static String nowAsKst() {
        return nowAsKstToFormat();
    }

    public static String nowAsKst(String format) {
        try {
            return ZonedDateTime.now(KST).format(DateTimeFormatter.ofPattern(format));
        } catch (DateTimeParseException e) {
            return nowAsKstToFormat();
        }
    }

    public static String nowAsKstToFormat() {
        return ZonedDateTime.now(KST).format(DateTimeFormatter.ofPattern(yyyyMMddHHmm));
    }

    public static String convertBetween(Long from) {
        return convertBetween(from, Instant.now().getEpochSecond());
    }

    public static String convertBetween(Long from, Long to) {

        Instant fromSecond = Instant.ofEpochSecond(from);
        Instant toSecond = Instant.ofEpochSecond(to);
        Duration duration = Duration.between(fromSecond, toSecond);

        return convertTimeStr(duration);
    }

    public static String convertDuration(Long at) {

        Instant instant = Instant.ofEpochSecond(at);
        Duration duration = Duration.ofSeconds(instant.getEpochSecond());

        return convertTimeStr(duration);
    }

    private String convertTimeStr(Duration duration) {

        int days = (int) duration.toDays();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();

        if (days >= 365) {
            return days / 365 + "년";
        } else if (days > 0) {
            return days + "일";
        } else if (hours > 0) {
            return hours + "시간";
        } else if (minutes > 0 || seconds > 0) {
            return minutes + "분 " + seconds + "초";
        } else if (seconds > 0) {
            return seconds + "초";
        }

        return "시간 없음";
    }

}
