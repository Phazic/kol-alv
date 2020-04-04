package com.googlecode.logVisualizer.logData.turn;

import com.googlecode.logVisualizer.parser.UsefulPatterns;

/**
 * This immutable class is a representation of the free runaway usage.
 */
public final class FreeRunaways {
    private static final String SLASH = "/";

    private static final String FREE_RETREATS_STRING = "free retreats";

    private final int numberOfAttemptedRunaways;

    private final int numberOfSuccessfulRunaways;

    /**
     * Creates a new FreeRunaways instance with the given number of runaways and
     * successful runaways.
     * 
     * @param numberOfAttemptedRunaways Number of runaways
     * @param numberOfSuccessfulRunaways Number of successful runaways
     * @throws IllegalArgumentException
     *             if either numberOfSuccessfulUsages or numberOfAttemptedUsages
     *             is below zero; if numberOfSuccessfulUsages is greater than
     *             numberOfAttemptedUsages
     */
    public FreeRunaways(
                        final int numberOfAttemptedRunaways, final int numberOfSuccessfulRunaways) {
        if (numberOfAttemptedRunaways < 0 || numberOfSuccessfulRunaways < 0)
            throw new IllegalArgumentException("Number of runaways must not be below 0.");
        if (numberOfSuccessfulRunaways > numberOfAttemptedRunaways)
            throw new IllegalArgumentException("Number of successful usages must not be below number of usages.");

        this.numberOfAttemptedRunaways = numberOfAttemptedRunaways;
        this.numberOfSuccessfulRunaways = numberOfSuccessfulRunaways;
    }

    /**
     * @return The number of attempted runaway usages.
     */
    public int getNumberOfAttemptedRunaways() {
        return numberOfAttemptedRunaways;
    }

    /**
     * @return The number of successful runaway usages.
     */
    public int getNumberOfSuccessfulRunaways() {
        return numberOfSuccessfulRunaways;
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(25);

        str.append(numberOfSuccessfulRunaways);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(SLASH);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(numberOfAttemptedRunaways);
        str.append(UsefulPatterns.WHITE_SPACE);
        str.append(FREE_RETREATS_STRING);

        return str.toString();
    }

    @Override
    public boolean equals(
                          final Object o) {
        if (o == this)
            return true;

        if (o == null)
            return false;

        if (o instanceof FreeRunaways)
            return ((FreeRunaways) o).getNumberOfSuccessfulRunaways() == numberOfSuccessfulRunaways;

        return false;
    }

    @Override
    public int hashCode() {
        int result = 687;
        result = 31 * result + super.hashCode();
        result = 31 * result + numberOfSuccessfulRunaways;

        return result;
    }
}