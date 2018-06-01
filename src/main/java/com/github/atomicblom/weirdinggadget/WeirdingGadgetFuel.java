package com.github.atomicblom.weirdinggadget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeirdingGadgetFuel {
	private static final Pattern compile = Pattern.compile("^(?:(?<domain>.*):)?(?<item>.+)(?:@(?<metadata>[0-9]*))?\\+(?<time>\\d*)(?<timeindicator>[hHdDmM])$");

	public final  String domain;
	public final String item;
	public final int metadata;
	public final long ticks;
	public final boolean ignoreMetadata;

	public WeirdingGadgetFuel(String domain, String item, long ticks)
	{
		this.domain = domain;
		this.item = item;
		this.metadata = -1;
		this.ticks = ticks;
		this.ignoreMetadata = true;
	}

	public WeirdingGadgetFuel(String domain, String item, int metadata, long ticks)
	{
		this.domain = domain;
		this.item = item;
		this.metadata = metadata;
		this.ticks = ticks;
		this.ignoreMetadata = false;
	}

	public static WeirdingGadgetFuel fromConfig(String configString) throws Exception {

		final Matcher matcher = compile.matcher(configString);
		if (!matcher.find()) {
			throw new Exception("Input did not match expected format");
		}

		final String domain = matcher.group("domain");
		final String item = matcher.group("item");
		if (item == null || item.isEmpty()) {
			throw new Exception("Could not parse item name");
		}

		final String metadataString = matcher.group("metadata");
		final int metadata = metadataString != null ? Integer.parseInt(metadataString) : -1;
		final String timeString = matcher.group("time");
		if (timeString == null || timeString.isEmpty()) {
			throw new Exception("Could not parse time name");
		}

		int time = timeString != null ? Integer.parseInt(timeString) : 0;
		final String timeindicator = matcher.group("timeindicator");
		if (timeindicator == null || timeindicator.isEmpty()) {
			throw new Exception("Could not parse time indicator name");
		}

		if ("m".equals(timeindicator.toLowerCase())) {
			time *= 20 * 60;
		} else if ("h".equals(timeindicator.toLowerCase())) {
			time *= 20 * 60 * 60;
		} else if ("d".equals(timeindicator.toLowerCase())) {
			time *= 20 * 60 * 60 * 24;
		}



		if (metadata >= 0) {
			return new WeirdingGadgetFuel(
					domain == null || domain.isEmpty() ? "minecraft" : domain.toLowerCase(),
					item.toLowerCase(),
					metadata,
					time
			);
		} else {
			return new WeirdingGadgetFuel(
					domain == null || domain.isEmpty() ? "minecraft" : domain.toLowerCase(),
					item.toLowerCase(),
					time
			);
		}
	}

	public String getDomain()
	{
		return domain;
	}

	public String getItem()
	{
		return item;
	}

	public int getMetadata()
	{
		return metadata;
	}

	public String toString() {
		long time = (ticks / 20 * 60);
		char units = 'm';
		long hours = (ticks / 20 * 60 * 60);
		if (hours > 0)
		{
			time = hours;
			units = 'h';
		}
		long days = (ticks / 20 * 60 * 60 * 24);
		if (days > 0) {
			time = days;
			units = 'd';
		}


		return domain + ':' + item + '@' + metadata + '+' + time + units;
	}
}
