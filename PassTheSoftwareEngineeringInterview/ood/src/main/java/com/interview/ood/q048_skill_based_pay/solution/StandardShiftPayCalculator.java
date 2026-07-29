package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

// Phase 1 implementation: flat hourlyRate x hoursWorked, no tiers. hoursWorked is derived
// from the shift's own Duration fields (total - break), converted to decimal hours only
// here, at the one place a rate is actually applied.
public class StandardShiftPayCalculator implements ShiftPayCalculator {

    // hoursWorked in the returned record is a DISPLAY figure only (4dp, for the breakdown a
    // caller shows a colleague). It is deliberately NOT used to compute pay — rounding hours
    // first and then multiplying by the rate is double-rounding, and produces a wrong pay
    // figure on non-15-minute-multiple shifts (e.g. a 17-minute break at £10.50/h: rounding
    // hours first gives £2.97; the mathematically exact £2.975 rounds to £2.98). Pay is
    // instead computed as (minutesWorked x hourlyRate) / 60, rounded ONCE at PAY_SCALE — the
    // multiply is exact BigDecimal arithmetic, and BigDecimal.divide(scale, roundingMode)
    // rounds the true mathematical quotient, so there is no intermediate truncation.
    private static final int HOURS_SCALE = 4;
    private static final int PAY_SCALE = 2;
    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);

    @Override
    public CalculatedShiftPay calculate(Shift shift, BigDecimal hourlyRate) {
        Duration worked = shift.totalDuration().minus(shift.breakDuration());
        BigDecimal minutesWorked = BigDecimal.valueOf(worked.toMinutes());

        BigDecimal pay = minutesWorked.multiply(hourlyRate)
                .divide(MINUTES_PER_HOUR, PAY_SCALE, RoundingMode.HALF_UP);
        BigDecimal hoursWorked = minutesWorked.divide(MINUTES_PER_HOUR, HOURS_SCALE, RoundingMode.HALF_UP);

        return new CalculatedShiftPay(hoursWorked, pay);
    }
}
