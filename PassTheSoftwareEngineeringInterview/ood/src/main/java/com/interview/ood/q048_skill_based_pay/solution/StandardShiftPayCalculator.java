package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

// Phase 1 implementation: flat hourlyRate x hoursWorked, no tiers. hoursWorked is derived
// from the shift's own Duration fields (total - break), converted to decimal hours only
// here, at the one place a rate is actually applied.
public class StandardShiftPayCalculator implements ShiftPayCalculator {

    // Extra scale on the intermediate hours-worked figure avoids compounding rounding error
    // before the final multiply; PAY_SCALE (2) matches real currency precision (pence) for
    // the number that's actually paid out.
    private static final int HOURS_SCALE = 4;
    private static final int PAY_SCALE = 2;

    @Override
    public CalculatedShiftPay calculate(Shift shift, BigDecimal hourlyRate) {
        Duration worked = shift.totalDuration().minus(shift.breakDuration());
        BigDecimal hoursWorked = BigDecimal.valueOf(worked.toMinutes())
                .divide(BigDecimal.valueOf(60), HOURS_SCALE, RoundingMode.HALF_UP);
        BigDecimal pay = hoursWorked.multiply(hourlyRate).setScale(PAY_SCALE, RoundingMode.HALF_UP);
        return new CalculatedShiftPay(hoursWorked, pay);
    }
}
