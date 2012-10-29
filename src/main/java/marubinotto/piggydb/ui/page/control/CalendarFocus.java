package marubinotto.piggydb.ui.page.control;

import marubinotto.util.Assert;
import marubinotto.util.time.DateTime;
import marubinotto.util.time.Interval;

public class CalendarFocus {

	public static abstract class Type extends org.apache.commons.lang.enums.Enum {

		public static final Type MONTH = new Type("month") {
			@Override
			public Interval toInterval(DateTime dateTime) {
				return dateTime.toMonth();
			}
			@Override
			public String format(Interval interval) {
				return interval.getStartInstant().format("yyyy/MM");
			}
		};

		public static final Type DAY = new Type("day") {
			@Override
			public Interval toInterval(DateTime dateTime) {
				return dateTime.toDayInterval();
			}
			@Override
			public String format(Interval interval) {
				return interval.getStartInstant().format("yyyy/MM/dd");
			}
		};

		private Type(String name) {
			super(name);
		}

		public abstract Interval toInterval(DateTime dateTime);
		public abstract String format(Interval interval);
	}

	private Interval interval;
	private Type type;

	private CalendarFocus() {
	}

	public static final String FORMAT_MONTH = "yyyyMM";
	public static final String FORMAT_DAY = "yyyyMMdd";

	public static CalendarFocus parseString(String string) {
		if (string == null) return null;

		CalendarFocus focus = new CalendarFocus();
		if (string.length() == FORMAT_MONTH.length()) {
			focus.select(new DateTime(string, FORMAT_MONTH), Type.MONTH);
		}
		else if (string.length() == FORMAT_DAY.length()) {
			focus.select(new DateTime(string, FORMAT_DAY), Type.DAY);
		}
		else {
			throw new IllegalArgumentException();
		}
		return focus;
	}

	public void select(DateTime dateTime, Type type) {
		Assert.Arg.notNull(dateTime, "dateTime");
		Assert.Arg.notNull(type, "type");

		this.interval = type.toInterval(dateTime);
		this.type = type;
	}

	public boolean isSelected(DateTime dateTime, Type type) {
		Assert.Arg.notNull(dateTime, "dateTime");
		Assert.Arg.notNull(type, "type");

		if (!type.equals(this.type)) return false;
		return this.interval.containsInstant(dateTime);
	}

	public Interval toInterval() {
		return this.interval;
	}
	
	@Override
	public String toString() {
		return this.type.format(this.interval);
	}
}
