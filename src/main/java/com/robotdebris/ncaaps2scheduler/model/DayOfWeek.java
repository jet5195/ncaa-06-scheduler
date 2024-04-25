package com.robotdebris.ncaaps2scheduler.model;

public enum DayOfWeek {
	MONDAY(0), TUESDAY(1), WEDNESDAY(2), THURSDAY(3), FRIDAY(4), SATURDAY(5), SUNDAY(6);

	private final int dayIndex;

	DayOfWeek(int dayIndex) {
		this.dayIndex = dayIndex;
	}

	public static DayOfWeek toEnum(int dayIndex) {
		for (DayOfWeek day : DayOfWeek.values()) {
			if (day.dayIndex == dayIndex) {
				return day;
			}
		}
		throw new IllegalArgumentException("Invalid day index: " + dayIndex);
	}

	public int getDayIndex() {
		return dayIndex;
	}
}
