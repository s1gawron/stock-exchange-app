const TIME_ZONE_PATTERN: RegExp = /(Z|[+-]\d{2}:?\d{2})$/;

/**
 * The backend sends dates without a time zone, in UTC (its clock is Clock.systemUTC()). Without a time zone, the browser would read them as
 * local time, so "Z" is added before parsing.
 */
export function parseServerDate(date: string): Date {
    return new Date(TIME_ZONE_PATTERN.test(date) ? date : `${date}Z`);
}

export function formatServerDate(date: string | null): string {
    return date === null ? "-" : parseServerDate(date).toLocaleString();
}
