package org.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FlightFilter {

    /**
     Вылет до текущего момента времени.
     **/
    public static List<Flight> departureToCurrentPointTime(List<Flight> list){
        LocalDateTime localDateTime = LocalDateTime.now();
        return list.stream()
                .filter(flight -> flight.getSegments().stream()
                        .allMatch(segment -> segment.getDepartureDate().isAfter(localDateTime)))
                .collect(Collectors.toList());

    }
    /**
     Сегменты с датой прилёта раньше даты вылета.
    **/
    public static List<Flight> segmentsWithArrivalDateEarlierThanDepartureDate(List<Flight> list){


        return list.stream()
                .filter(flight -> flight.getSegments().stream()
                        .noneMatch(segment -> segment.getArrivalDate().isBefore(segment.getDepartureDate())))
                .collect(Collectors.toList());
    }
    /**
     Перелеты, где общее время, проведённое на земле, превышает два часа.
     **/
    public static List<Flight> filterFlightsWithGroundTimeExceedingTwoHours(List<Flight> flights) {
        return flights.stream()
                .filter(flight -> {
                    List<Segment> segments = flight.getSegments();
                    if (segments.size() <= 1) {
                        return true;
                    }
                    Duration totalGroundTime = Duration.ZERO;
                    for (int i = 0; i < segments.size() - 1; i++) {
                        Segment currentSegment = segments.get(i);
                        Segment nextSegment = segments.get(i + 1);
                        Duration groundTime = Duration.between(currentSegment.getArrivalDate(), nextSegment.getDepartureDate());
                        totalGroundTime = totalGroundTime.plus(groundTime);
                    }
                    return totalGroundTime.toHours() <= 2; // Исключаем рейсы, у которых суммарное время на земле превышает 2 часа
                })
                .collect(Collectors.toList());
    }
}
